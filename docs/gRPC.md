# gRPC

## Introduction

gRPC, ou gRPC Remote Procedure Call, est un framework open source développé par Google 
pour la communication synchrone entre services.
Il permet à une application (client) d'appeler directement des méthodes sur un serveur distant
comme si elles étaient des objets locaux.

> **IMPORTANT**: 
> 1. gRPC n'est pas destiné à remplacer les API REST pour la communication externe (client-serveur)
> 2. gRPC est principalement utilisé pour la communication interne entre microservices
> 3. gRPC est optimisé pour la performance et la faible latence pour la communication entre services (OneToOne) 
> 4. gRPC et REST sont synchrones, mais gRPC supporte aussi le streaming bidirectionnel
> 5. gRPC utilise HTTP/2 et Protobuf, tandis que REST utilise HTTP/1.1 et JSON
> 6. gRPC et REST communiquent de manière synchrone, utile pour les appels directs entre services. 
> 7. Kafka est asynchrone, idéal pour la communication déconnectée et les architectures basées sur des événements. (OneTOMany)


### Fonctionnement
gRPC est basé sur un modèle d'appel de procédure distante (RPC) qui repose sur trois piliers technologiques :

- **Protocol Buffers (Protobuf) pour la définition de services et la sérialisation** :
  Au lieu d'utiliser des formats lisibles par l'homme comme JSON ou XML, gRPC utilise Protobuf. 
  Le développeur définit un "contrat" pour l'API dans un fichier .proto 
  qui spécifie les services (c'est-à-dire les fonctions que le serveur expose) 
  et la structure des messages (les données de requête et de réponse). 
  Un compilateur (protoc) génère ensuite du code client et serveur (des "stubs") 
  dans le langage de programmation souhaité. Ces stubs gèrent automatiquement 
  la sérialisation des données en un format binaire compact.

- **HTTP/2 pour le transport** : 
  Contrairement à REST qui utilise principalement HTTP/1.1, gRPC est construit sur HTTP/2. 
  Cela apporte plusieurs fonctionnalités clés pour la performance, notamment le multiplexage 
  (plusieurs requêtes peuvent être envoyées sur une seule connexion TCP) et la compression des en-têtes (Header Compression).

- **Appel de méthode** : 
  Lorsqu'un client effectue un appel de méthode gRPC, il appelle la méthode locale générée par Protobuf. 
  Ce stub client prend les paramètres, les sérialise en un message Protobuf binaire, puis envoie ce message au serveur via HTTP/2. 
  Le stub serveur reçoit la requête, désérialise le message, et appelle la méthode correspondante implémentée par le développeur. 
  La réponse est traitée de manière inverse.

gRPC prend en charge quatre types de méthodes RPC :

- **Unary RPC** : Le client envoie une seule requête et reçoit une seule réponse.

- **Server streaming RPC** : Le client envoie une requête et reçoit un flux de réponses du serveur.

- **Client streaming RPC** : Le client envoie un flux de requêtes et reçoit une seule réponse.

- **Bidirectional streaming RPC** : Les deux parties s'envoient des flux de messages simultanément.

### Pourquoi gRPC est-il rapide ?

La vitesse de gRPC est le résultat de ses choix technologiques fondamentaux, 
qui le rendent plus performant que les API REST basées sur JSON/HTTP/1.1 
pour les communications internes entre microservices.

1. **Format de données binaire (Protobuf)** : 
  Les charges utiles de Protobuf sont beaucoup plus petites que leurs équivalents JSON ou XML. 
  Protobuf stocke les données dans un format binaire compact, ce qui réduit considérablement 
  la quantité de données à transférer sur le réseau. 
  De plus, la sérialisation et la désérialisation des données binaires sont plus rapides 
  pour les ordinateurs que le traitement du texte lisible par l'homme (comme JSON).

2. **Utilisation de HTTP/2** : 
  Le protocole HTTP/2 offre des avantages significatifs par rapport à HTTP/1.1 :

  - **Multiplexage** : 
    HTTP/1.1 exige une nouvelle connexion TCP pour chaque requête parallèle. 
    HTTP/2 permet d'envoyer plusieurs requêtes et réponses simultanément sur une seule connexion TCP persistante. 
    Cela réduit la latence et l'overhead lié à l'établissement de nouvelles connexions.

  - **Compression des en-têtes (HPACK)** : 
    Les en-têtes HTTP/2 sont compressés, ce qui réduit la taille des données envoyées avec chaque requête. 
    Dans une architecture de microservices où les en-têtes sont souvent répétés, cela permet de gagner en efficacité.

  - **Streaming** : 
    HTTP/2 supporte nativement le streaming bidirectionnel, ce qui est une fonctionnalité clé pour les applications en temps réel (jeux, chat) 
    et est plus efficace que les solutions basées sur des requêtes multiples avec HTTP/1.1.

En résumé, la combinaison d'un format de données binaire et d'un protocole de transport optimisé rend gRPC très performant, 
en particulier dans les environnements où la latence et l'efficacité de la bande passante sont primordiales, 
comme les architectures de microservices.

## Mise en place

gRPC sera utilisé pour la communication interne entre les microservices de l'application.

![Microservices : gRPC communication](/docs/images/gRPC_scalability.jpg)

## Testing

On peut utiliser [gRPCurl](https://github.com/fullstorydev/grpcurl/releases) pour tester les services gRPC en ligne de commande.


### gRPCurl
You can download the binary for your OS from the [Github: gRPCurl](https://github.com/fullstorydev/grpcurl/releases)

#### Basic commands

```bash
# Lister les services disponibles
grpcurl -plaintext localhost:50051 list

# Afficher les méthodes d'un service spécifique
grpcurl -plaintext localhost:50051 list my.package.MyService

# Appeler une méthode unary RPC
grpcurl -plaintext -d '{"param1": "value1", "param2": "value2"}' localhost:50051 my.package.MyService/MyMethod  

# Appeler une méthode avec streaming RPC
grpcurl -plaintext -d '{"param1": "value1"}' localhost:50051 my.package.MyService/StreamMethod

# Utiliser un fichier JSON pour les données d'entrée
grpcurl -plaintext -d @ localhost:50051 my.package.MyService/MyMethod < request.json
```

