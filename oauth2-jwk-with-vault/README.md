## Installing Vault with Docker

### 1. Pull the Vault Image
First, pull the specified version of the Vault image (1.13.3):
```bash
docker pull vault:1.13.3
```

### 2. Run the Vault Container
Run the Vault container in development mode:
```bash
docker run --cap-add=IPC_LOCK -d --name=dev-vault vault:1.13.3
```

### 3. View Vault Startup Logs
View the Vault startup logs to obtain the Root Token:
```bash
docker logs -f dev-vault
```

Example log output:
```
             Api Address: http://127.0.0.1:8200
                     Cgo: disabled
         Cluster Address: https://127.0.0.1:8201
              Listener 1: tcp (addr: "127.0.0.1:8200", cluster address: "127.0.0.1:8201", max_request_duration: "1m30s", max_request_size: "33554432", tls: "disabled")
               Log Level: info
                   Mlock: supported: false, enabled: false
           Recovery Mode: false
                 Storage: inmem
                 Version: Vault v1.13.3

WARNING! dev mode is enabled! In this mode, Vault runs entirely in-memory
and starts unsealed with a single unseal key. The root token is already
authenticated to the CLI, so you can immediately begin using Vault.

You may need to set the following environment variable:

    $ export VAULT_ADDR='http://127.0.0.1:8200'

The unseal key and root token are displayed below in case you want to
seal/unseal the Vault or re-authenticate.

Unseal Key: 1+yv+v5mz+aSCK67X6slL3ECxb4UDL8ujWZU/ONBpn0=
Root Token: s.XmpNPoi9sRhYtdKHaQhkHP6x

Development mode should NOT be used in production installations!
```

### 4. Configure the Vault Client
Start a new terminal session and enter the container:
```bash
docker exec -it dev-vault /bin/sh
```

Set the Vault address:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
```

Set the Root Token as an environment variable:
```bash
export VAULT_TOKEN="s.XmpNPoi9sRhYtdKHaQhkHP6x"
```

### 5. Verify the Vault Server is Running
Within the container, run the following command to check the server status:
```bash
vault status
```

If you encounter an error like:
```
Error checking seal status: Get "https://127.0.0.1:8200/v1/sys/seal-status": http: server gave HTTP response to HTTPS client
```
Ensure that the `VAULT_ADDR` environment variable is configured correctly.

### 6. Enable the Transit Engine
```shell
vault secrets enable transit
```

### 7. Create a Signing-Supported Key (e.g., RSA-2048)
Use the `rsa-2048` type of key to support signing operations:
```bash
vault write -f transit/keys/oauth2 type="rsa-2048"
```

## Configure Vault Token in Services
Configure the Root Token in the `application.yml` files of both the authorization-service and resource-service as `${vault_token}`. After starting the services, visit [http://127.0.0.1:8070/client/test](http://127.0.0.1:8070/client/test) in your browser.

## References
- [Vault Installation Guide](https://learn.hashicorp.com/tutorials/vault/getting-started-install?in=vault/getting-started)
- [Vault Development Server Guide](https://learn.hashicorp.com/tutorials/vault/getting-started-dev-server?in=vault/getting-started)
- [Vault Docker Image Details](https://registry.hub.docker.com/_/vault)
