# PF — Projeto de Software

Java 25, Spring Boot 4.1.2, PostgreSQL, React/Vite e autenticação Auth0.

## Executar o backend

Com Docker Desktop aberto, na raiz do projeto:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.2"
$env:DB_PASSWORD = 'pf-local'
docker compose up -d --wait
.\mvnw.cmd -B verify
.\mvnw.cmd spring-boot:run
```

API local: `http://localhost:8085`. PostgreSQL local: `localhost:5434`, banco `pfprojsoftdb`, usuário `usuario`. A senha acima é somente de desenvolvimento local.

## Executar o frontend

```sh
cd frontend
npm ci
npm run dev
```

Frontend local: `http://localhost:5173`. Para compilar: `npm run build`.

O proxy de desenvolvimento está em `frontend/vite.config.js`. O destino publicado é definido em `frontend/vercel.json`. Os identificadores públicos do Auth0 podem ser personalizados pelas variáveis `VITE_AUTH0_DOMAIN`, `VITE_AUTH0_CLIENT_ID` e `VITE_AUTH0_AUDIENCE`.

## Configuração

O backend usa `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `AUTH0_ISSUER_URI`, `AUTH0_AUDIENCE`, `APP_ALLOWED_ORIGINS` e `PORT`. Configuração: `src/main/resources/application.properties`. Não versionar credenciais.

## API

Base de conhecimentos da empresa. Todas as rotas de artigos exigem access token do Auth0.

| Rota | Permissão | Descrição |
| --- | --- | --- |
| `GET /artigos` | autenticado | Lista todos os artigos. |
| `POST /artigos` | role ADMIN | Cria um artigo com `titulo`, `descricao` e `prioridade`. O e-mail do criador vem do token. Retorna 201. |
| `DELETE /artigos/{id}` | role ADMIN | Deleta um artigo. Retorna 204, ou 404 se não existir. |
| `GET /actuator/health` | público | Confirma aplicação e banco. |

Exemplo de corpo do `POST /artigos`:

```json
{ "titulo": "Como abrir um chamado", "descricao": "Passo a passo do suporte.", "prioridade": "Alta" }
```

Resposta: `id`, `titulo`, `descricao`, `prioridade` e `emailCriador`. Dados inválidos retornam 400.

Execute `scripts/validar.ps1` para verificar saúde e bloqueio de acesso anônimo.

## Publicação

O push na `main` executa `.github/workflows/deploy.yml`: build do frontend, testes Java, publicação da imagem Docker e deploy na AWS.

- API: http://3.227.9.20:8080
- Frontend: https://pf-projsoft-front.vercel.app

Na AWS, a porta pública 8080 é encaminhada à porta 8085 do container `pf-projsoft`. Os dados ficam no PostgreSQL `postgres-pf-projsoft`. O deploy do frontend é feito pela Vercel a partir da pasta `frontend`.
