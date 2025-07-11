# **Payment processing REST API**

___

## Aleksa Lukic <br><br> Računarski fakultet <br><br> Belgrade, Serbia

___

### Возможности:
- Регистрация и вход пользователя
- Создание и закрытие счётов
- Переводы между счетами
- История транзакций
- Получение баланса по счёту
- Swagger-документация

___

## Нюансы реализации

- **Balance:** Spring Boot приложение которое работает на localhost:8080, запуск через docker-compose файл который запускает базу данных и само приложение в Docker`
- **Balance:** BigDecimal использовать было нельзя, а float и double с погрешностями в арифметикe, поэтому принял решение хранить как BigInt в центах. На фронтенде для представления `long eur = balance/100` <br> `long cents = balance%100;`
- **Configuration:** Все beans сконфигурированы в `AppConfiguration` с аннотацией @Configuration
- **База данных:** PostgeSQL который запускается локально в Docker container, в приложении используется `JDBC Template`. Не успел настроить миграции к сожалению.
- **Без Spring Security:** Вся логика сессий и CSRF реализована вручную через фильтры.
- **Сессии:** Сессии хранятся с базе проиндексированные по `id` и `is_valid` для скорости. Нужно было их кэшировать в Redis, но не успел добавить.
- **CSRF защита:** Используется уникальный токен для каждой сессии. Проверка происходит во всех POST/PUT/DELETE запросах, кроме `/login` и `/register`.
- **Cookie:** Для хранения ID сессии используется `SESSION_ID` cookie с флагом `HttpOnly` и `Secure` для защиты от XSS и обеспечения передачи по https. Cookie сессионный и живет до закрытия браузера в рамках безопасности.
- **CORS:** Сделал CORS фильтр для фронтенд приложения чтобы протестировать на localhost, но в итоге не было времени делать фронтенд...
- **Transactions:** Для переводов используется уровень изоляции `Serializable` чтобы избежать аномалий. Все остальные транзакции по умолчанию - `Read commited` c `MVCC` (default уровень в Postgres).  
- **Swagger:** В проекте подключен `OpenAPI (Swagger UI)` для визуальной документации всех эндпоинтов.
- **Логирование:** Использован `SLF4J + Logback` с логами уровня INFO/WARN для действий пользователя. В консоль логи писать конечно нельзя, но не было времени подключить условно `ELK` к сожалению.
- **Тесты:** Не успел написать unit тесты. Все протестировал через Postman.

___

## Уровень использования LLM

- Генерация boilerplate кода (нпр. большая часть документации)
- Консольные команды которые забылись
- Сгенерировать какой-нибудь dependency для pom.xml (быстрее чем вручную искать)
- Случайно возникающие теоретические вопросы

## Запуск приложения

#### Docker-сompose сначала запускает БД и создает схему и таблицы с помощью скрипта в db-config/db-init.sql и потом запускает Spring Boot приложение. 
#### Environment variables читаются из .env файла, тк так проще для теста.

```
./mvnw clean package && docker compose up --build
```
