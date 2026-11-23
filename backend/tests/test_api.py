import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.asyncio
async def test_health():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        response = await ac.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["currency"] == "INR"

@pytest.mark.asyncio
async def test_search():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        response = await ac.get("/api/search?q=cyberpunk")
    assert response.status_code == 200
    items = response.json()
    assert isinstance(items, list)
    if items:
        first = items[0]
        assert "title" in first
        assert "stores" in first
        assert any(s["store"] == "Steam" for s in first["stores"])

@pytest.mark.asyncio
async def test_library_and_stats():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        response = await ac.get("/api/library/unified")
    assert response.status_code == 200
    data = response.json()
    assert data["total_games"] > 0
    assert data["steam_count"] > 0
    assert data["epic_count"] > 0
    assert "duplicates" in data

@pytest.mark.asyncio
async def test_free_games():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        response = await ac.get("/api/free-games")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
