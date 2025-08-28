# Authentication & Autorization 

## Introduction

![Authentification & Autorization](images/auth_architecture.jpg)

Le système d'authentification et d'autorisation ci-dessus est conçu 
pour sécuriser l'accès aux ressources d'une application web. 
Il utilise des jetons JWT (JSON Web Tokens) pour authentifier les utilisateurs et gérer leurs permissions.
L'authentification sera gérée par le service `auth-service` 
tandis que l'autorisation sera gérée par le `api-gateway`.

## Authentification

Une requête d'authentification est envoyée par un client 
(par exemple, un navigateur web ou une application mobile) au service `auth-service`. 
Cette requête contient les informations d'identification de l'utilisateur, 
telles que le nom d'utilisateur et le mot de passe.
Le service ``api-gateway`` redirige cette requête vers le service `auth-service` 
qui vérifie les informations d'identification de l'utilisateur. 
Si les informations sont valides, le service `auth-service` génère un jeton JWT 
contenant les informations de l'utilisateur et ses permissions, puis renvoie un jeton JWT au client.
Le client stocke ce jeton JWT (généralement dans le stockage local ou les cookies) 
et l'inclut dans les en-têtes des requêtes ultérieures pour accéder aux ressources protégées.

## Autorisation

Le processus d'autorisation sera le suivant:
1. Lorsque le client tente d'accéder à une ressource protégée, 
il envoie une requête au `api-gateway` avec le jeton JWT dans les en-têtes.
2. Le service `api-gateway` extrait le jeton JWT de la requête 
et demande au service `auth-service` de valider le jeton JWT en vérifiant sa signature et sa date d'expiration. 
3. Si le jeton est valide, le `api-gateway` vérifie les permissions de l'utilisateur contenues 
dans le jeton pour déterminer s'il est autorisé à accéder à la ressource demandée.

