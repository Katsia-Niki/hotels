# Hotels REST API

RESTful API приложение для работы с отелями.

## Технологии

- Java 21
- Maven
- Spring Boot
- Spring Data JPA
- Liquibase
- H2

## Запуск

```bash
mvn spring-boot:run
```

Приложение запускается на порту `8092`.

## Swagger

- UI: `http://localhost:8092/swagger-ui.html`
- OpenAPI: `http://localhost:8092/v3/api-docs`

## Основные endpoints

- `GET /property-view/hotels`
- `GET /property-view/hotels/{id}`
- `GET /property-view/search`
- `POST /property-view/hotels`
- `POST /property-view/hotels/{id}/amenities`
- `GET /property-view/histogram/{param}`
