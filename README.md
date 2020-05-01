# user-management


1 - docker and docker-compose

2 - start services:
  - user-management
  - db-user-management

        docker-compose up 
#

2 - start services hydra oauthkeeper and keto

    docker-compose -f docker-compose-hydra.yaml up


3 - create client hydra

    docker-compose -f docker-compose-hydra.yaml exec hydra \
        hydra clients create \
        --endpoint http://127.0.0.1:4445 \
        --id auth-code-client \
        --secret secret \
        --grant-types client_credentials,authorization_code,refresh_token \
        --response-types code,id_token \
        --scope openid,offline \
        --callbacks http://localhost:9090/api-users/auth-callback
        
4 - register user
        
        
        curl --location --request POST 'http://localhost:9090/api-users/register' \
        --header 'Content-Type: application/json' \
        --data-raw '{
        	"name": "Lucas",
        	"email": "lucascoelhosilvacs@gmail.com",
        	"password": "123"
        }'


5 - create policies 

    curl --location --request POST 'http://localhost:9090/api-users/keto' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "actions": ["get", "post", "put"],
        "description": "modify and access users",
        "effect": "allow",
        "resources": ["user-management:user"],
        "subjects": ["lucascoelhosilvacs@gmail.com"],
        "conditions": null
    }'
    
  