# user-management

sample application using Ory open source projects

<!-- [![Ory](https://www.ory.sh/docs/img/docs/ory-ecosystem.png)](https://www.ory.sh/) -->

#### Requirements

- docker and docker-compose

#### Installation
- docker and docker-compose
```
 docker-compose build
```


#### Start
- start services:
  - user-management
  - db-user-management
```
docker-compose up
```
 
#### Start services Ory (Hydra OauthKeeper Keto)

2 - start services hydra oauthkeeper and keto
```
docker-compose -f docker-compose-hydra.yaml build
docker-compose -f docker-compose-hydra.yaml up
```

_________________________


#### example usage

3 - create client hydra

    docker-compose -f docker-compose-hydra.yaml exec hydra \
        hydra clients create \
        --endpoint http://127.0.0.1:4445 \
        --id auth-code-client \
        --secret secret \
        --grant-types client_credentials,authorization_code,refresh_token \
        --response-types code,id_token \
        --scope openid,offline \
        --callbacks http://localhost:9090/api-user-management/auth-callback
  

__________________________________________________
  
4 - register user
        
        curl --location --request POST 'http://localhost:9090/api-user-management/register' \
        --header 'Content-Type: application/json' \
        --data-raw '{
        	"name": "admin",
        	"email": "admin@admin.com",
        	"password": "123"
        }'


5 - create policies 

    curl --location --request POST 'http://localhost:9090/api-user-management/keto/policies' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "description": "policy admin",
      "subjects": ["admin"],
      "effect": "allow",
      "resources": [
          "user-management:user"
      ],
      "actions": ["get", "post", "put", "delete"],
      "conditions": null
    }'
    
 6 - create role
    
    curl --location --request POST 'http://localhost:9090/api-user-management/keto/roles' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "id":"admin",
      "members": [
        "admin@admin.com"
      ]
    }'
    
