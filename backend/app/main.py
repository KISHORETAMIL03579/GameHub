from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers import games, offers, library, accounts

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="Backend API for GameHub — cross-store PC game search, INR price comparison, free games tracker, and library aggregator."
)

# Enable CORS for Android client and emulator (10.0.2.2) access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(games.router)
app.include_router(offers.router)
app.include_router(library.router)
app.include_router(accounts.router)

@app.get("/health", tags=["System"])
async def health_check():
    return {
        "status": "healthy",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "currency": settings.DEFAULT_CURRENCY,
        "country": settings.DEFAULT_COUNTRY_CODE
    }
