# ЮрАнализ — Система анализа юридических документов

Веб-приложение для анализа юридических документов с модулем поиска похожих прецедентов на основе алгоритма TF-IDF.

## Технологический стек

| Компонент | Технология |
|-----------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite, TailwindCSS |
| База данных | PostgreSQL 15 |
| Миграции | Liquibase |
| Аутентификация | JWT + GitHub OAuth2 |
| Контейнеризация | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Деплой | Render |

## Архитектура

```
legal-analysis/
├── backend/          # Spring Boot REST API (Java 21)
├── frontend/         # React 18 + TypeScript SPA
├── docker-compose.yml
├── render.yaml
└── .github/workflows/
```

## Быстрый старт (Docker Compose)

### Требования
- Docker 24+
- Docker Compose 2.20+

### Запуск

```bash
# Клонировать репозиторий
git clone https://github.com/your-username/legal-analysis.git
cd legal-analysis

# Запустить все сервисы
docker compose up --build -d

# Открыть приложение
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Остановка

```bash
docker compose down
```

## Локальная разработка без Docker

### Backend

Требования: Java 21, Maven 3.9, PostgreSQL 15

```bash
# Создать БД
psql -U postgres -c "CREATE DATABASE legal_analysis;"

# Настроить переменные окружения или application.yml
export DATABASE_URL=jdbc:postgresql://localhost:5432/legal_analysis
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Запустить
cd backend
./mvnw spring-boot:run
```

### Frontend

Требования: Node.js 20+

```bash
cd frontend
cp .env.example .env
# Отредактировать .env при необходимости
npm install
npm run dev
```

## Тестирование

```bash
# Unit + Integration тесты
cd backend
./mvnw test

# Только unit тесты
./mvnw test -Dtest="*ServiceTest,*StrategyTest"
```

## Деплой на Render

### 1. Backend (Web Service)

| Параметр | Значение |
|---------|---------|
| Build Command | `cd backend && ./mvnw clean package -DskipTests` |
| Start Command | `java -XX:MaxRAMPercentage=75.0 -jar backend/target/legal-analysis-backend-0.0.1-SNAPSHOT.jar` |
| Health Check | `/actuator/health` |

**Переменные окружения:**
```
DATABASE_URL=<автоматически из PostgreSQL addon>
JWT_SECRET=<случайная строка>
GITHUB_CLIENT_ID=<ваш GitHub OAuth Client ID>
GITHUB_CLIENT_SECRET=<ваш GitHub OAuth Client Secret>
FRONTEND_URL=https://<your-frontend>.onrender.com
```

### 2. Frontend (Static Site)

| Параметр | Значение |
|---------|---------|
| Build Command | `cd frontend && npm install && npm run build` |
| Publish Directory | `frontend/dist` |

**Переменные окружения:**
```
VITE_API_URL=https://<your-backend>.onrender.com
VITE_GITHUB_CLIENT_ID=<тот же GitHub OAuth Client ID>
```

### 3. База данных PostgreSQL

Создайте PostgreSQL addon на Render — бесплатный план имеет 1GB хранилища.

### 4. Настройка домена (hoster.by)

1. В панели Render откройте ваш Static Site → Settings → Custom Domains
2. Добавьте ваш домен: `yourdomain.by`
3. Скопируйте CNAME значение
4. В панели hoster.by добавьте DNS запись:
   - Тип: `CNAME`
   - Имя: `@` или `www`
   - Значение: `<ваш-сайт>.onrender.com`
5. Подождите 5-30 минут для распространения DNS

## Демо-данные

После первого запуска автоматически загружаются:
- **25 прецедентов** по категориям: Гражданское право, Трудовые споры, Семейное право, Договорное право, Жилищное право, Административное право, Потребительские споры
- **7 категорий** юридических дел
- **12 тегов** для классификации
- Тестовый аккаунт администратора

### Тестовый вход
```
Email: admin@legal-analysis.com
Пароль: password
```

### Пример тестового документа

Создайте файл `test.txt` со следующим содержимым и загрузите на главной странице:

```
Иск о взыскании задолженности по договору займа

Истец обратился в суд с требованием о взыскании суммы займа
в размере 150 000 рублей. Между сторонами был заключен договор займа.
Ответчик обязательства по возврату займа не исполнил.
Просим взыскать сумму основного долга и проценты за пользование.
```

Система найдет похожие прецеденты из базы данных.

## API документация

После запуска бэкенда Swagger UI доступен по адресу:
```
http://localhost:8080/swagger-ui.html
```

### Основные эндпоинты

```
POST   /api/v1/auth/register        - Регистрация
POST   /api/v1/auth/login           - Вход
POST   /api/v1/auth/refresh         - Обновление токена
GET    /api/v1/auth/me              - Текущий пользователь

POST   /api/v1/documents            - Загрузка документа
GET    /api/v1/documents            - Список документов
DELETE /api/v1/documents/{id}       - Удаление документа

POST   /api/v1/search/analyze/{id}  - Анализ документа
GET    /api/v1/search/results/{id}  - Результаты анализа
GET    /api/v1/search/history       - История поисков

GET    /api/v1/precedents           - Список прецедентов
GET    /api/v1/precedents/{id}      - Прецедент по ID
GET    /api/v1/precedents/search    - Поиск по тексту

POST   /api/v1/favorites/{id}       - Добавить в избранное
GET    /api/v1/favorites            - Список избранного
DELETE /api/v1/favorites/{id}       - Удалить из избранного
```

## Паттерны проектирования

| Паттерн | Применение |
|---------|-----------|
| **Strategy** | `TextAnalysisStrategy` — различные алгоритмы анализа текста |
| **Factory** | `DocumentParserFactory` — создание парсеров для .txt и .docx |
| **Builder** | Lombok `@Builder` для сложных DTO и сущностей |
| **Facade** | `GitHubApiClient` — упрощение работы с GitHub API |
| **Repository** | Spring Data JPA repositories для всех сущностей |

## Структура базы данных

**12 таблиц:**
- `users` — пользователи системы
- `roles` — роли (USER, ADMIN)
- `categories` — категории юридических дел
- `tags` — теги для классификации
- `documents` — загруженные документы
- `document_tags` — связь документов и тегов
- `precedents` — база прецедентов
- `precedent_tags` — связь прецедентов и тегов
- `document_precedents` — результаты поиска (матч документ-прецедент)
- `search_history` — история поисков
- `favorites` — избранные прецеденты пользователей
- `annotations` — заметки к документам

## Лицензия

MIT
