# Деплой на Render (без Docker)

Бэкенд деплоится как **Java Web Service**, фронтенд — как **Static Site**.
Оба сервиса берутся из одного GitHub репозитория.

---

## Шаг 1. Настройка GitHub OAuth App для продакшена

> Если у тебя уже есть один OAuth App (настроенный на localhost) — создай отдельный для Render.

1. Открой https://github.com/settings/developers → **OAuth Apps** → **New OAuth App**
2. Заполни:
   - **Application name**: `ЮрАнализ (Production)`
   - **Homepage URL**: `https://legal-analysis-frontend.onrender.com`
   - **Authorization callback URL**: `https://legal-analysis-frontend.onrender.com/auth/github/callback`
3. Нажми **Register application**
4. Скопируй **Client ID** и сгенерируй **Client Secret** (кнопка "Generate a new client secret")

> Если хочешь использовать один OAuth App и для локалки и для Render — это **невозможно** напрямую, т.к. GitHub разрешает один callback URL. Используй два отдельных приложения.

---

## Шаг 2. Деплой через render.yaml (Blueprint)

В репозитории уже есть файл `render.yaml` — Render подхватит его автоматически.

### 2.1. Создай новый Blueprint на Render

1. Открой https://dashboard.render.com
2. Нажми **New** → **Blueprint**
3. Подключи свой GitHub репозиторий `cursach_vas2`
4. Render найдёт `render.yaml` и покажет список сервисов для создания:
   - `legal-analysis-backend` (Java Web Service)
   - `legal-analysis-frontend` (Static Site)
   - `legal-analysis-db` (PostgreSQL)
5. Нажми **Apply**

### 2.2. Заполни секретные переменные

После создания Blueprint, Render попросит заполнить переменные с пометкой `sync: false`.

Для сервиса **legal-analysis-backend** нужно вручную задать:
- `GITHUB_CLIENT_SECRET` = `1b3e92bf99096099fdbd2dd80f2808b8de374d0f`

> Остальные переменные (`GITHUB_CLIENT_ID`, `FRONTEND_URL`, `DATABASE_URL`, `JWT_SECRET`) уже заданы в `render.yaml` и заполнятся автоматически.

---

## Шаг 3. Проверка URLs

После деплоя убедись, что URL сервисов совпадают с тем, что прописано в `render.yaml`:

| Сервис | URL по умолчанию |
|--------|-----------------|
| Бэкенд | `https://legal-analysis-backend.onrender.com` |
| Фронтенд | `https://legal-analysis-frontend.onrender.com` |

Если Render дал другие URL (например, с суффиксом вроде `-abc1`), нужно обновить:
- В `render.yaml`: переменную `FRONTEND_URL` и `VITE_API_URL`
- В настройках GitHub OAuth App: **Authorization callback URL**

---

## Шаг 4. Переменные окружения (справочник)

### Бэкенд (`legal-analysis-backend`)

| Переменная | Значение | Откуда |
|-----------|---------|--------|
| `DATABASE_URL` | `postgres://user:pass@host/db` | Автоматически из БД addon |
| `JWT_SECRET` | случайная строка 64 hex | Генерируется Render |
| `GITHUB_CLIENT_ID` | `Iv23litDJyhMjHkL1oDL` | Из `render.yaml` |
| `GITHUB_CLIENT_SECRET` | `1b3e92...` | **Задать вручную** |
| `FRONTEND_URL` | `https://legal-analysis-frontend.onrender.com` | Из `render.yaml` |

### Фронтенд (`legal-analysis-frontend`)

| Переменная | Значение | Откуда |
|-----------|---------|--------|
| `VITE_API_URL` | `https://legal-analysis-backend.onrender.com` | Из `render.yaml` |
| `VITE_GITHUB_CLIENT_ID` | `Iv23litDJyhMjHkL1oDL` | Из `render.yaml` |

> `VITE_*` переменные **запекаются** в сборку при `npm run build`. Если изменишь URL — нужно пересобрать фронтенд (Render сделает это автоматически при следующем push).

---

## Шаг 5. Ручной деплой (без Blueprint)

Если Blueprint не подходит — создай сервисы вручную.

### 5.1. Создай PostgreSQL базу данных

1. Render Dashboard → **New** → **PostgreSQL**
2. Name: `legal-analysis-db`
3. Database: `legal_analysis`
4. User: `postgres`
5. Plan: **Free**
6. Нажми **Create Database**
7. Скопируй **Internal Database URL** (нужен для бэкенда)

### 5.2. Создай Web Service для бэкенда

1. Render Dashboard → **New** → **Web Service**
2. Подключи репозиторий
3. Заполни настройки:
   - **Name**: `legal-analysis-backend`
   - **Root Directory**: `backend`
   - **Environment**: `Java`
   - **Build Command**: `mvn package -DskipTests -B`
   - **Start Command**: `java -Xmx400m -jar target/legal-analysis-backend-0.0.1-SNAPSHOT.jar`
   - **Plan**: Free
4. В разделе **Environment Variables** добавь:
   ```
   DATABASE_URL = <Internal Database URL из шага 5.1>
   JWT_SECRET   = <любая случайная строка 64+ символа>
   GITHUB_CLIENT_ID     = Iv23litDJyhMjHkL1oDL
   GITHUB_CLIENT_SECRET = 1b3e92bf99096099fdbd2dd80f2808b8de374d0f
   FRONTEND_URL = https://legal-analysis-frontend.onrender.com
   ```
5. **Health Check Path**: `/actuator/health`
6. Нажми **Create Web Service**

### 5.3. Создай Static Site для фронтенда

1. Render Dashboard → **New** → **Static Site**
2. Подключи тот же репозиторий
3. Заполни:
   - **Name**: `legal-analysis-frontend`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
   - **Plan**: Free
4. В разделе **Environment Variables**:
   ```
   VITE_API_URL          = https://legal-analysis-backend.onrender.com
   VITE_GITHUB_CLIENT_ID = Iv23litDJyhMjHkL1oDL
   ```
5. В разделе **Redirects/Rewrites** добавь правило:
   - Source: `/*`
   - Destination: `/index.html`
   - Action: **Rewrite**
6. Нажми **Create Static Site**

---

## Шаг 6. Проверка деплоя

1. Дождись окончания сборки (логи видны в Render Dashboard)
2. Проверь бэкенд: `https://legal-analysis-backend.onrender.com/actuator/health`
   → должен ответить `{"status":"UP"}`
3. Открой фронтенд: `https://legal-analysis-frontend.onrender.com`
4. Войди через email: `admin@legal-analysis.com` / `password`
5. Проверь GitHub OAuth

---

## Важно: Free Tier ограничения

- Render **выключает сервисы** при отсутствии запросов ~15 минут
- **Первый запрос** после простоя будет долгим (30–60 сек — "холодный старт")
- PostgreSQL на Free Tier **удаляется через 90 дней** — делай бэкап данных
- RAM лимит: 512 MB (поэтому в startCommand стоит `-Xmx400m`)

---

## Обновление деплоя

Каждый `git push` в ветку `main` автоматически запускает пересборку обоих сервисов на Render (если настроен Auto-Deploy).

Ручной деплой: Render Dashboard → сервис → кнопка **Manual Deploy** → **Deploy latest commit**.
