# Diagramas de arquitetura e sequências

Diagramas Mermaid. Renderizar em visualizador compatível com Mermaid (por exemplo, GitHub) ao apresentar o projeto.

## Contexto

```mermaid
flowchart LR
    requester[Solicitante]
    coordinator[Responsável / Administrador]
    app[Gestão de Recursos Educacionais]
    mongo[(MongoDB replica set)]
    notice[Canal de notificação simulada]

    requester -->|consulta, reserva, acompanha| app
    coordinator -->|administra, aprova, movimenta| app
    app <-->|recursos, usuários, reservas, auditoria| mongo
    app -->|persiste evento simulado| notice
    notice --> mongo
```

## Componentes

```mermaid
flowchart TB
    subgraph clients[Clientes]
        web[Interface Thymeleaf / Bootstrap]
        api[Cliente HTTP / API REST]
    end
    subgraph application[Spring Boot]
        security[Spring Security]
        webctrl[Controladores Web]
        apictrl[Controladores REST]
        services[Serviços de domínio]
        audit[AuditLogService]
        notify[SimulatedNotificationService]
        repositories[Repositórios Spring Data MongoDB]
    end
    mongo[(MongoDB)]

    web --> security --> webctrl
    api --> security --> apictrl
    webctrl --> services
    apictrl --> services
    services --> audit
    services --> notify
    services --> repositories
    audit --> repositories
    notify --> repositories
    repositories --> mongo
```

## Reserva e bloqueio concorrente

```mermaid
sequenceDiagram
    actor S as Solicitante
    participant C as ReservationApiController
    participant R as ReservationService
    participant DB as MongoDB
    S->>C: POST /api/reservations
    C->>R: create(request, ator)
    R->>DB: validar recursos e manutenção
    R->>DB: salvar reserva
    R->>DB: inserir slots com índice único
    alt slots livres
        DB-->>R: inserção confirmada
        R->>DB: gravar auditoria e notificação
        R-->>C: reserva aceita
        C-->>S: 201 Created
    else slot já ocupado
        DB-->>R: DuplicateKeyException
        R->>DB: registrar conflito evitado
        C-->>S: 409 Conflict
    end
```

## Retirada e devolução

```mermaid
sequenceDiagram
    actor A as Administrador
    participant C as AdminOperationsApiController
    participant S as MaterialCheckoutService
    participant DB as MongoDB
    A->>C: POST /api/admin/checkouts
    C->>S: withdraw(reserva, recurso, ator)
    S->>DB: validar reserva aprovada, tipo e manutenção
    S->>DB: persistir retirada (responsável/data)
    S->>DB: registrar auditoria e notificação simulada
    C-->>A: 201 Created
    A->>C: POST /api/admin/checkouts/{id}/return
    C->>S: returnMaterial(id, ator)
    S->>DB: persistir devolução (responsável/data)
    S->>DB: registrar auditoria e notificação simulada
    C-->>A: 200 OK
```

## Decisão de recurso restrito

```mermaid
sequenceDiagram
    actor U as Solicitante
    actor A as Administrador
    participant API as API de reservas
    participant RS as ReservationService
    participant DB as MongoDB
    U->>API: POST /api/reservations
    API->>RS: create(...)
    RS->>DB: reserva PENDING_APPROVAL + slots
    RS-->>U: 201 Created (pendente)
    A->>API: POST /api/reservations/{id}/approve
    API->>RS: decide(id, aprovar, ator)
    RS->>DB: atualizar estado + auditoria/notificação
    API-->>A: 200 OK (APPROVED)
```
