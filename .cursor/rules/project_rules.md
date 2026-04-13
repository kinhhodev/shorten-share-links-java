# Project Rules

## Architecture
- Follow Clean Architecture (Controller → Service → Repository)
- Separate domain logic khỏi infrastructure
- Không viết business logic trong Controller

## Backend (Spring Boot)
- Use RESTful API conventions
- Validate input using @Valid
- Use DTO (không expose entity trực tiếp)
- Global Exception Handler (@ControllerAdvice)
- Use Redis cho caching + rate limit

## Security
- Prevent XSS (escape output, validate input)
- Prevent SQL Injection (JPA only, no raw query)
- Implement Rate Limiting (Redis)
- Sanitize all user inputs

## Performance
- API response < 100ms (target)
- Use caching for redirect short links
- Use async when possible

## Code Quality
- Naming rõ ràng
- Method < 50 lines
- Class single responsibility
- Viết log cho critical flow

## Database
- Use PostgreSQL
- Proper indexing (slug, topicId)
- Use UUID cho public ID

## Frontend (React)
- Use functional components + hooks
- Use folder structure: feature-based
- API call tách riêng (services layer)
- State management: React Query / Zustand

## UI
- Style: Neobrutalism
- Reusable components