# 🎭 Excuse Generator (Java & Spring Boot)

An interactive web application built with **Java 21** and **Spring Boot 3** that generates contextual excuses in 5 languages across multiple categories from a verified database (`excuses.json`).

## ✨ Features

- **Multi-language Support (5 languages)**:
  - 🇵🇱 Polish (`pl`)
  - 🇬🇧 English (`en`)
  - 🇪🇸 Spanish (`es`)
  - 🇫🇷 French (`fr`)
  - 🇸🇦 Arabic (`ar`) with full **Right-To-Left (RTL)** layout support
- **Category Filtering**:
  - ✨ All Categories
  - 🥂 Social Gatherings (`social`)
  - 💼 Work (`work`)
  - 🎓 School (`school`)
  - 🏋️ Fitness & Gym (`fitness`)
- **Full UI Localization**: Headings, buttons, labels, and category names adapt instantly to the selected language without page reload.
- **Copy to Clipboard**: One-click excuse copy with instant visual checkmark and localized feedback toast.
- **Responsive Modern UI**: Ambient glowing dark theme with glassmorphism card, entrance animations, and keyboard shortcuts (<kbd>Space</kbd> to generate).
- **RESTful API**: Clean JSON endpoints for languages, categories, translations, and random excuse generation.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (or compatible JDK)
- **Maven 3.9+** (or use `./mvnw`)

### Build the Project
```bash
mvn clean package
```
or with wrapper:
```bash
./mvnw clean package
```

### Run the Application
```bash
mvn spring-boot:run
```
or run the packaged JAR:
```bash
java -jar target/excuse-generator-1.0.0.jar
```

The application will start on **http://localhost:8080** by default (customizable via `PORT` environment variable or `-Dserver.port=...`).

### Run Tests
```bash
mvn test
```

---

## 📡 REST API Documentation

### 1. Supported Languages
- **Endpoint**: `GET /api/languages`
- **Response**:
```json
[
  { "code": "pl", "name": "Polish", "nativeName": "Polski", "flag": "🇵🇱", "dir": "ltr" },
  { "code": "en", "name": "English", "nativeName": "English", "flag": "🇬🇧", "dir": "ltr" },
  { "code": "es", "name": "Spanish", "nativeName": "Español", "flag": "🇪🇸", "dir": "ltr" },
  { "code": "fr", "name": "French", "nativeName": "Français", "flag": "🇫🇷", "dir": "ltr" },
  { "code": "ar", "name": "Arabic", "nativeName": "العربية", "flag": "🇸🇦", "dir": "rtl" }
]
```

### 2. Localized Categories
- **Endpoint**: `GET /api/categories?lang={code}`
- **Parameters**: `lang` (optional, default: `en`)
- **Response**:
```json
[
  { "id": "all", "name": "All Categories", "icon": "✨", "count": 80 },
  { "id": "social", "name": "Social Gatherings", "icon": "🥂", "count": 20 },
  { "id": "work", "name": "Work", "icon": "💼", "count": 20 },
  { "id": "school", "name": "School", "icon": "🎓", "count": 20 },
  { "id": "fitness", "name": "Fitness", "icon": "🏋️", "count": 20 }
]
```

### 3. Random Excuse Generation
- **Endpoint**: `GET /api/excuse/random?category={catId}&lang={code}`
- **Parameters**:
  - `category` (optional, default: `all`)
  - `lang` (optional, default: `en`)
- **Response**:
```json
{
  "text": "Kurier napisał, że będzie 'między 14:00 a 21:00' i muszę cały czas nasłuchiwać domofonu.",
  "categoryId": "social",
  "categoryName": "Spotkania towarzyskie",
  "categoryIcon": "🥂",
  "lang": "pl",
  "totalAvailable": 20
}
```

### 4. UI Translations Dictionary
- **Endpoint**: `GET /api/ui-translations`
- **Response**: Localized labels, buttons, subtitles for instant in-browser switching.
