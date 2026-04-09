from pydantic import BaseModel, EmailStr, Field
from typing import Optional


class EmailReplyRequest(BaseModel):
    recipient_email: EmailStr
    recipient_name: Optional[str] = None
    context: str = Field(..., min_length=10, description="Customer email or interaction summary")
    tone: Optional[str] = Field(default="professional", pattern="^(professional|friendly|assertive|empathetic)$")


class EmailReplyResponse(BaseModel):
    recipient_email: str
    subject: str
    body: str
    model_used: str
