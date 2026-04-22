# Запуск проекта локально (без Docker)

## Требования

| Инструмент | Версия | Примечание |
|-----------|--------|------------|
| Java JDK | 21 | Eclipse Temurin рекомендуется — https://adoptium.net |
| Maven | 3.9+ | Достаточно того, что поставляется с IntelliJ IDEA |
| Node.js | 20+ | https://nodejs.org |
| PostgreSQL | 17 или 18 | Должна быть установлена и запущена |

---

## Шаг 1. Убедись что JAVA_HOME указывает на JDK 21

Провери в PowerShell:
```powershell
echo $env:JAVA_HOME
java -version
```

Если `JAVA_HOME` указывает на JDK 17 (или другую версию, не 21) — исправь:

1. Win+R → `sysdm.cpl` → **Дополнительно** → **Переменные среды**
2. В блоке **Системные переменные** найди `JAVA_HOME` → **Изменить**
3. Укажи путь к JDK 21, например:
   `C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot`
4. Нажми ОК и **перезапусти PowerShell**

---

## Шаг 2. Создай базу данных

### Через pgAdmin (если psql не в PATH)
1. Открой pgAdmin
2. Раскрой сервер → правой кнопкой на **Databases** → **Create → Database**
3. В поле **Database** напиши `legal_analysis` → **Save**

### Через psql (полный путь)
```powershell
# PostgreSQL 17
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE legal_analysis;"

# PostgreSQL 18
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -c "CREATE DATABASE legal_analysis;"
```

---

## Шаг 3. Настрой переменные окружения для бэкенда

Скопируй шаблон:
```
backend/.env.example  →  backend/.env
```

Отредактируй `backend/.env` — впиши свой пароль от PostgreSQL:
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/legal_analysis?sslmode=disable
DB_USERNAME=postgres
DB_PASSWORD=твой_пароль

JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

GITHUB_CLIENT_ID=твой_локальный_github_client_id
GITHUB_CLIENT_SECRET=твой_локальный_github_client_secret

FRONTEND_URL=http://localhost:5173
```

> Если PostgreSQL слушает на порту **5433** — замени `5432` на `5433` в `DATABASE_URL`.

---

## Шаг 4. Запуск бэкенда

### Способ А — скрипт run.ps1 (рекомендуется)

Создай файл `backend/run.ps1`:
```powershell
Get-Content "$PSScriptRoot\.env" | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
mvn spring-boot:run
```

Запускай:
```powershell
cd D:\cursach_vas2\backend
.\run.ps1
```

### Способ Б — вручную в PowerShell

```powershell
cd D:\cursach_vas2\backend

Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}

mvn spring-boot:run
```

### Способ В — IntelliJ IDEA

1. Открой `LegalAnalysisApplication.java` → **Run → Edit Configurations**
2. В поле **Environment variables** добавь все переменные из `backend/.env`
   (или установи плагин **EnvFile** и укажи путь к файлу)
3. Нажми **Run**

**Бэкенд запустится на:** http://localhost:8080

Проверь: http://localhost:8080/actuator/health → должен ответить `{"status":"UP"}`

> При первом запуске Liquibase автоматически создаст все таблицы и наполнит базу тестовыми данными.

---

## Шаг 5. Настройка GitHub OAuth App для локалки

1. Открой https://github.com/settings/developers → **OAuth Apps** → **New OAuth App**
2. Заполни:
   - **Application name**: `ЮрАнализ Local`
   - **Homepage URL**: `http://localhost:5173`
   - **Authorization callback URL**: `http://localhost:5173/auth/github/callback`
3. Нажми **Register application**
4. Скопируй **Client ID** и сгенерируй **Client Secret**
5. Впиши их в `backend/.env` в поля `GITHUB_CLIENT_ID` и `GITHUB_CLIENT_SECRET`

---

## Шаг 6. Запуск фронтенда

Скопируй шаблон:
```
frontend/.env.example  →  frontend/.env.local
```

Содержимое `frontend/.env.local`:
```env
VITE_API_URL=http://localhost:8080
VITE_GITHUB_CLIENT_ID=твой_локальный_github_client_id
```

Запусти:
```powershell
cd D:\cursach_vas2\frontend
npm install
npm run dev
```

**Фронтенд запустится на:** http://localhost:5173

---

## Шаг 7. Проверка

1. Открой http://localhost:5173
2. Войди с тестовым аккаунтом: `admin@legal-analysis.com` / `password`
3. Проверь вход через GitHub

---

## Частые проблемы

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `release version 21 not supported` | JAVA_HOME указывает на старый JDK | Исправь JAVA_HOME (Шаг 1) |
| `database "legal_analysis" does not exist` | База не создана | Создай базу (Шаг 2) |
| `no password was provided` | Переменные не загружены | Перезапусти терминал и загрузи .env (Шаг 4) |
| `Connection refused` на порту 5432 | PostgreSQL не запущена или другой порт | Запусти PostgreSQL, проверь порт в pgAdmin |
| `redirect_uri_mismatch` в GitHub OAuth | Неверный callback URL в OAuth App | Callback должен быть точно `http://localhost:5173/auth/github/callback` |
| `psql` не найден | psql не в PATH | Используй полный путь (Шаг 2) или pgAdmin |
