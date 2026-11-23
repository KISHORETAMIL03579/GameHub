import os
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    APP_NAME: str = "GameHub API"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True
    DEFAULT_CURRENCY: str = "INR"
    DEFAULT_COUNTRY_CODE: str = "IN"

    # Steam Web API (optional, can fallback to storefront API)
    STEAM_API_KEY: str = os.getenv("STEAM_API_KEY", "")
    STEAM_STORE_BASE_URL: str = "https://store.steampowered.com/api"
    STEAM_STORE_SEARCH_URL: str = "https://store.steampowered.com/api/storesearch"
    
    # Epic Games Store Endpoints
    EPIC_GRAPHQL_URL: str = "https://graphql.epicgames.com/graphql"
    EPIC_FREE_GAMES_URL: str = "https://store-site-backend-static-ipv4.ak.epicgames.com/freeGamesPromotions"

    # Cache TTL (in seconds)
    CACHE_TTL_SECONDS: int = 300

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

settings = Settings()
