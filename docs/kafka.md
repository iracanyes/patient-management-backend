# Kafka

Kafka est une **plateforme de streaming d'événements distribuée**. 
Il sert de "bus de messages" à haute performance, permettant aux applications de publier 
et de s'abonner à des flux de données en temps réel. 
Il est principalement utilisé pour la construction de pipelines de données, l'analyse en temps réel 
et la communication asynchrone entre microservices.

---

### Comment fonctionne Kafka ?

Kafka fonctionne selon un modèle de **publication-abonnement** (publish-subscribe) et repose sur plusieurs concepts clés :

* **Producteurs (Producers)** : Les applications qui **publient** des messages ou des événements dans Kafka.
* **Consommateurs (Consumers)** : Les applications qui **s'abonnent** aux messages et les traitent.
* **Brokers** : Les serveurs Kafka qui forment le cluster. Ils stockent les messages et gèrent les requêtes des producteurs et des consommateurs.
* **Sujets (Topics)** : Les catégories ou flux de messages. Les producteurs écrivent sur des sujets, et les consommateurs lisent à partir de sujets.
* **Partitions** : Un sujet est divisé en plusieurs partitions. Chaque partition est un journal d'événements ordonné et immuable. Cela permet la parallélisation et la tolérance aux pannes. Les messages dans une partition sont toujours dans un ordre séquentiel.
* **Décalage (Offset)** : C'est un identifiant unique pour chaque message dans une partition. Les consommateurs gardent une trace de leur position de lecture par l'offset.

Le flux de travail est le suivant :
1.  Les **producteurs** envoient des messages à un **sujet** spécifique sur un **broker**.
2.  Le broker ajoute le message à une **partition** de ce sujet.
3.  Les **consommateurs** lisent les messages séquentiellement à partir des partitions auxquelles ils sont abonnés, en gardant en mémoire leur dernier `offset`.

---

### Avantages et Inconvénients de Kafka

### Avantages ✅

* **Haute performance et débit élevé** : Conçu pour gérer des millions de messages par seconde grâce à son architecture distribuée, l'utilisation de la mémoire du système d'exploitation et son format de stockage efficace.
* **Durabilité et persistance** : Les messages sont écrits sur le disque et répliqués sur plusieurs brokers, ce qui garantit qu'ils ne sont pas perdus en cas de panne d'un serveur.
* **Évolutivité (Scalabilité)** : Vous pouvez ajouter facilement de nouveaux brokers au cluster pour augmenter la capacité de stockage et de traitement. Vous pouvez également ajouter des partitions pour augmenter le parallélisme.
* **Tolérance aux pannes** : Les brokers sont répliqués, et si un broker tombe en panne, un autre prend le relais.
* **Faible latence** : Les messages peuvent être publiés et consommés en temps quasi réel.
* **Flexibilité** : Permet la communication asynchrone entre services. Les producteurs et consommateurs n'ont pas besoin d'être en ligne en même temps.

---

### Inconvénients ❌

* **Complexité de l'installation et de la gestion** : Configurer et maintenir un cluster Kafka de production peut être complexe, notamment pour la gestion des partitions, la réplication et la surveillance.
* **Dépendance à ZooKeeper** : Historiquement, Kafka dépendait de ZooKeeper pour la gestion des métadonnées du cluster. Bien que cela soit en train de changer avec le protocole KRaft (Kafka Raft), de nombreuses installations existantes utilisent encore ZooKeeper.
* **Absence de support de requêtes complexes** : Kafka est un bus de messages, pas une base de données. Il n'offre pas de fonctionnalités de requêtes complexes comme les jointures. Pour cela, vous devez déplacer les données vers une base de données ou utiliser des technologies comme Kafka Streams.
* **Courbe d'apprentissage** : La maîtrise de ses concepts (brokers, partitions, réplication, offsets, etc.) peut prendre du temps pour les nouveaux utilisateurs.
* **Absence de messages transactionnels** : Il n'y a pas de garantie de l'atomicité sur des transactions complexes impliquant plusieurs topics.

## Kafka: Message format

Kafka acceptes des messages sous différents formats, les plus courants sont :
* **JSON** : Facile à lire et à écrire, mais peut être verbeux et moins performant pour les gros volumes de données.
* **Avro** : Format binaire compact avec un schéma, idéal pour les gros volumes de données. Nécessite un registre de schéma.
* **Protobuf** : Format binaire efficace avec un schéma, développé par Google. Nécessite également un registre de schéma.
* **String** : Simple chaîne de caractères, utile pour des messages très simples.
* **Bytes** : Format binaire brut, offre une grande flexibilité mais nécessite une gestion explicite du format des données.
* **XML** : Format textuel structuré, mais moins courant en raison de sa verbosité et de ses performances inférieures par rapport à JSON ou Avro.