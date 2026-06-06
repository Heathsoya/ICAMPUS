from pathlib import Path
from dotenv import load_dotenv

# Load .env from project root (if present) to populate environment variables for settings
root = Path(__file__).resolve().parent.parent
load_dotenv(dotenv_path=root / ".env")

from .settings import *
