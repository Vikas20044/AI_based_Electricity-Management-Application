"""
Deep Learning Regression Models: LSTM & Temporal Fusion Transformer (TFT)
AI-Based Household Electricity Management Application

Implements:
1. Long Short-Term Memory (LSTM) Model for Temporal Electricity Load Forecasting.
2. Temporal Fusion Transformer (TFT) Model utilizing Gated Residual Networks (GRN),
   Gated Linear Units (GLU), and Interpretable Multi-Head Self-Attention.
Both models provide a scikit-learn compatible estimator API (.fit, .predict)
and support seamless serialization with joblib.
"""

import copy
import io
import os
import random
import numpy as np
import pandas as pd
import torch
import torch.nn as nn
from torch.utils.data import TensorDataset, DataLoader
from sklearn.base import BaseEstimator, RegressorMixin

# ---------------------------------------------------------------------------
# 1. Gated Linear Unit (GLU) and Gated Residual Network (GRN) Building Blocks
# ---------------------------------------------------------------------------

class GatedLinearUnit(nn.Module):
    """
    Gated Linear Unit: GLU(x) = Linear1(x) * sigmoid(Linear2(x))
    Provides non-linear adaptive gating over representation channels.
    """
    def __init__(self, d_model: int):
        super().__init__()
        self.fc = nn.Linear(d_model, d_model * 2)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        val, gate = self.fc(x).chunk(2, dim=-1)
        return val * torch.sigmoid(gate)


class GatedResidualNetwork(nn.Module):
    """
    Gated Residual Network (GRN):
    Fundamental component of Temporal Fusion Transformers providing non-linear
    processing with adaptive gating and residual connections.
    """
    def __init__(
        self,
        input_dim: int,
        hidden_dim: int,
        output_dim: int,
        dropout: float = 0.1,
        context_dim: int = None
    ):
        super().__init__()
        self.fc1 = nn.Linear(input_dim, hidden_dim)
        self.context_fc = nn.Linear(context_dim, hidden_dim, bias=False) if context_dim else None
        self.elu = nn.ELU()
        self.fc2 = nn.Linear(hidden_dim, hidden_dim)
        self.glu = GatedLinearUnit(hidden_dim)
        self.proj_out = nn.Linear(hidden_dim, output_dim) if hidden_dim != output_dim else nn.Identity()
        self.skip = nn.Linear(input_dim, output_dim) if input_dim != output_dim else nn.Identity()
        self.layer_norm = nn.LayerNorm(output_dim)
        self.dropout = nn.Dropout(dropout)

    def forward(self, x: torch.Tensor, context: torch.Tensor = None) -> torch.Tensor:
        h = self.fc1(x)
        if self.context_fc is not None and context is not None:
            h = h + self.context_fc(context)
        h = self.elu(h)
        h = self.dropout(self.fc2(h))
        gated = self.glu(h)
        out = self.layer_norm(self.skip(x) + self.proj_out(gated))
        return out


# ---------------------------------------------------------------------------
# 2. Long Short-Term Memory (LSTM) Architecture
# ---------------------------------------------------------------------------

class LSTMNetwork(nn.Module):
    """
    PyTorch Neural Network for LSTM-based Electricity Demand Forecasting.
    Projects feature matrix into sequential representation, models temporal recurrence
    via multi-layer LSTM cells, and combines temporal hidden state with a static skip connection.
    """
    def __init__(
        self,
        input_dim: int,
        hidden_dim: int = 64,
        num_layers: int = 2,
        seq_len: int = 4,
        dropout: float = 0.15
    ):
        super().__init__()
        self.seq_len = seq_len
        self.hidden_dim = hidden_dim

        # Project flat feature vector into multi-step temporal sequence
        self.temporal_proj = nn.Sequential(
            nn.Linear(input_dim, seq_len * hidden_dim),
            nn.ReLU()
        )

        # Multi-layer LSTM
        self.lstm = nn.LSTM(
            input_size=hidden_dim,
            hidden_size=hidden_dim,
            num_layers=num_layers,
            batch_first=True,
            dropout=dropout if num_layers > 1 else 0.0
        )

        # Static residual path
        self.residual_fc = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.ReLU()
        )

        # Deep Regression Head
        self.regressor = nn.Sequential(
            nn.LayerNorm(hidden_dim * 2),
            nn.Linear(hidden_dim * 2, hidden_dim),
            nn.ReLU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, 32),
            nn.ReLU(),
            nn.Linear(32, 1)
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        batch_size = x.size(0)
        # (batch_size, seq_len, hidden_dim)
        seq = self.temporal_proj(x).view(batch_size, self.seq_len, self.hidden_dim)
        lstm_out, (hn, cn) = self.lstm(seq)

        # Last layer hidden state
        last_hidden = hn[-1]
        res = self.residual_fc(x)

        # Fuse temporal context with direct static features
        fused = torch.cat([last_hidden, res], dim=-1)
        pred = self.regressor(fused).squeeze(-1)
        return pred


# ---------------------------------------------------------------------------
# 3. Temporal Fusion Transformer (TFT) Architecture
# ---------------------------------------------------------------------------

class TemporalFusionTransformerNetwork(nn.Module):
    """
    PyTorch Implementation of Temporal Fusion Transformer (TFT)
    Incorporating:
    - Gated Residual Networks (GRN) for variable feature selection and gating.
    - Interpretable Multi-Head Self-Attention over temporal feature sequence.
    - Gated skip connections combining attended temporal representations with static household features.
    """
    def __init__(
        self,
        input_dim: int,
        d_model: int = 64,
        n_heads: int = 4,
        seq_len: int = 4,
        dropout: float = 0.15
    ):
        super().__init__()
        self.seq_len = seq_len
        self.d_model = d_model

        # Variable selection & temporal projection via GRN
        self.vsn_grn = GatedResidualNetwork(input_dim, d_model, d_model * seq_len, dropout=dropout)

        # Temporal Multi-Head Attention
        self.self_attn = nn.MultiheadAttention(
            embed_dim=d_model,
            num_heads=n_heads,
            dropout=dropout,
            batch_first=True
        )
        self.attn_norm = nn.LayerNorm(d_model)

        # Post-attention GRN
        self.post_attn_grn = GatedResidualNetwork(d_model, d_model, d_model, dropout=dropout)

        # Direct static / feature skip connection via GRN
        self.static_grn = GatedResidualNetwork(input_dim, d_model, d_model, dropout=dropout)

        # Multi-stage Output Prediction Head
        self.output_head = nn.Sequential(
            nn.LayerNorm(d_model * 2),
            nn.Linear(d_model * 2, d_model),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(d_model, 32),
            nn.GELU(),
            nn.Linear(32, 1)
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        batch_size = x.size(0)

        # Project features into variable-selected sequence (B, seq_len, d_model)
        temporal_seq = self.vsn_grn(x).view(batch_size, self.seq_len, self.d_model)

        # Temporal self-attention
        attn_out, _ = self.self_attn(temporal_seq, temporal_seq, temporal_seq)
        attended = self.attn_norm(temporal_seq + attn_out)

        # Mean pooling over temporal dimension followed by post-attn GRN
        temporal_rep = self.post_attn_grn(attended.mean(dim=1))

        # Direct static feature representation
        static_rep = self.static_grn(x)

        # Fuse temporal self-attention with static context
        fused = torch.cat([temporal_rep, static_rep], dim=-1)
        pred = self.output_head(fused).squeeze(-1)
        return pred


# ---------------------------------------------------------------------------
# 4. Scikit-Learn Compatible Estimator Wrappers
# ---------------------------------------------------------------------------

class BasePyTorchRegressor(BaseEstimator, RegressorMixin):
    """
    Base wrapper providing scikit-learn compatible fit and predict interfaces,
    standard mini-batch training loops, and joblib serialization.
    """
    def __init__(
        self,
        batch_size: int = 64,
        epochs: int = 45,
        lr: float = 0.003,
        weight_decay: float = 1e-4,
        random_state: int = 42
    ):
        self.batch_size = batch_size
        self.epochs = epochs
        self.lr = lr
        self.weight_decay = weight_decay
        self.random_state = random_state
        self.network = None
        self.input_dim_ = None

    def _build_network(self, input_dim: int) -> nn.Module:
        raise NotImplementedError

    def fit(self, X, y):
        # Set seeds
        torch.manual_seed(self.random_state)
        np.random.seed(self.random_state)
        random.seed(self.random_state)

        if isinstance(X, pd.DataFrame):
            X_arr = X.values.astype(np.float32)
        else:
            X_arr = np.asarray(X, dtype=np.float32)

        if isinstance(y, (pd.Series, pd.DataFrame)):
            y_arr = y.values.astype(np.float32).ravel()
        else:
            y_arr = np.asarray(y, dtype=np.float32).ravel()

        self.input_dim_ = X_arr.shape[1]
        self.network = self._build_network(self.input_dim_)
        self.network.train()

        dataset = TensorDataset(torch.from_numpy(X_arr), torch.from_numpy(y_arr))
        loader = DataLoader(dataset, batch_size=self.batch_size, shuffle=True)

        optimizer = torch.optim.AdamW(self.network.parameters(), lr=self.lr, weight_decay=self.weight_decay)
        criterion = nn.SmoothL1Loss(beta=2.0)
        scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=self.epochs, eta_min=1e-5)

        for epoch in range(self.epochs):
            self.network.train()
            for batch_x, batch_y in loader:
                optimizer.zero_grad()
                preds = self.network(batch_x)
                loss = criterion(preds, batch_y)
                loss.backward()
                torch.nn.utils.clip_grad_norm_(self.network.parameters(), max_norm=1.0)
                optimizer.step()
            scheduler.step()

        return self

    def predict(self, X) -> np.ndarray:
        if self.network is None:
            raise RuntimeError("Model has not been fitted yet. Call .fit() before .predict().")

        if isinstance(X, pd.DataFrame):
            X_arr = X.values.astype(np.float32)
        else:
            X_arr = np.asarray(X, dtype=np.float32)

        self.network.eval()
        with torch.no_grad():
            tensor_x = torch.from_numpy(X_arr)
            # Batch inference to manage memory
            preds_list = []
            loader = DataLoader(TensorDataset(tensor_x), batch_size=256, shuffle=False)
            for (bx,) in loader:
                p = self.network(bx).cpu().numpy().ravel()
                preds_list.append(p)

        preds = np.concatenate(preds_list)
        # Physical electricity consumption cannot be negative
        preds = np.clip(preds, 0.0, None)
        return preds

    def __getstate__(self):
        state = self.__dict__.copy()
        if self.network is not None:
            buf = io.BytesIO()
            torch.save(self.network.state_dict(), buf)
            state["network_state_dict"] = buf.getvalue()
            state["network"] = None
        return state

    def __setstate__(self, state):
        network_bytes = state.pop("network_state_dict", None)
        self.__dict__.update(state)
        if network_bytes is not None and self.input_dim_ is not None:
            self.network = self._build_network(self.input_dim_)
            buf = io.BytesIO(network_bytes)
            self.network.load_state_dict(torch.load(buf, weights_only=True))
            self.network.eval()


class LSTMModel(BasePyTorchRegressor):
    """
    Long Short-Term Memory (LSTM) Regressor for Electricity Forecasting.
    """
    def __init__(
        self,
        hidden_dim: int = 64,
        num_layers: int = 2,
        seq_len: int = 4,
        dropout: float = 0.15,
        batch_size: int = 64,
        epochs: int = 45,
        lr: float = 0.003,
        weight_decay: float = 1e-4,
        random_state: int = 42
    ):
        super().__init__(
            batch_size=batch_size,
            epochs=epochs,
            lr=lr,
            weight_decay=weight_decay,
            random_state=random_state
        )
        self.hidden_dim = hidden_dim
        self.num_layers = num_layers
        self.seq_len = seq_len
        self.dropout = dropout

    def _build_network(self, input_dim: int) -> nn.Module:
        return LSTMNetwork(
            input_dim=input_dim,
            hidden_dim=self.hidden_dim,
            num_layers=self.num_layers,
            seq_len=self.seq_len,
            dropout=self.dropout
        )


class TemporalFusionTransformerModel(BasePyTorchRegressor):
    """
    Temporal Fusion Transformer (TFT) Regressor for Electricity Forecasting.
    """
    def __init__(
        self,
        d_model: int = 64,
        n_heads: int = 4,
        seq_len: int = 4,
        dropout: float = 0.15,
        batch_size: int = 64,
        epochs: int = 45,
        lr: float = 0.003,
        weight_decay: float = 1e-4,
        random_state: int = 42
    ):
        super().__init__(
            batch_size=batch_size,
            epochs=epochs,
            lr=lr,
            weight_decay=weight_decay,
            random_state=random_state
        )
        self.d_model = d_model
        self.n_heads = n_heads
        self.seq_len = seq_len
        self.dropout = dropout

    def _build_network(self, input_dim: int) -> nn.Module:
        return TemporalFusionTransformerNetwork(
            input_dim=input_dim,
            d_model=self.d_model,
            n_heads=self.n_heads,
            seq_len=self.seq_len,
            dropout=self.dropout
        )

