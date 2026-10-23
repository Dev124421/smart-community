import json
from pathlib import Path

import joblib
from fastapi import FastAPI
from pydantic import BaseModel
from scipy.sparse import hstack

from preprocess import clean_text

BASE = Path(__file__).parent / "models"
word_vec, char_vec, models = joblib.load(BASE / "baseline.joblib")
thresholds = json.loads((BASE / "thresholds.json").read_text())
LABELS = list(models.keys())
MODEL_VERSION = "tfidf-logreg-v1"

app = FastAPI(title="Smart Community ML Service")


class Message(BaseModel):
    text: str


@app.get("/health")
def health():
    return {"status": "ok", "model_version": MODEL_VERSION}


@app.post("/predict")
def predict(msg: Message):
    clean = clean_text(msg.text)
    X = hstack([word_vec.transform([clean]), char_vec.transform([clean])]).tocsr()

    scores = {label: float(models[label].predict_proba(X)[0, 1]) for label in LABELS}

    outcome = "allow"
    for label, score in scores.items():
        reject_t = thresholds[label]["reject"]
        if reject_t is not None and score >= reject_t:
            outcome = "reject"
            break
        if score >= thresholds[label]["hold"]:
            outcome = "hold"

    top_label = max(scores, key=scores.get)
    return {
        "outcome": outcome,
        "top_label": top_label,
        "confidence": round(scores[top_label], 4),
        "scores": {k: round(v, 4) for k, v in scores.items()},
        "model_version": MODEL_VERSION,
    }