# Project_Dedale

Projet FoSyMa 2020-2021

Projet d'introduction aux systèmes multi-agents effectué au cours du master 1 ANDROIDE de janvier à juin 2021 dans le cadre de l'UE FOSYMA.

**Encadrement du projet : Cédric Herpson**

Le projet consiste à développer des comportements pour des agents en Java à l'aide de la bibliothèque _GraphStream_ et _Jade_.
Le code se base du code du projet [Dedale](dedale.gitlab.io/). Le but du projet est de créer des agents avec des comportements pour bloquer un agent ennemi sur le graphe en l'empêchant de se déplacer. Les agents n'ont pas de vision et trouve l'ennemi grâce à une trace orange autour de lui. Les agents se détectent entre eux en s'envoyant des signaux avec des distances limitées (sans destinataires précis car les positions ne sont pas connues) et en mettant à jour leur carte et les positions connu des agents.

Les fichiers suivant ont été ajoutés/modifiés pour définir le comportement des agents :
- *MapRepresentation.java*
- *FSMExploAgent.java*
- *YellowSetup.java*
- *ObservationBehaviour.java*
- *SignalBehaviour.java*

Dans le fichier *MapRepresentation.java*, des fonctions ont été faites pour travailler sur la graphe de la carte dans lequel les agents se déplacent. Cela permet par exemple de détecter le type de graphe (cycle ou arbre) ou encore de récuperer des noeuds du graphe pour l'exploration.

Dans le fichier *FSMExploAgent.java*, les agents sont inscrits dans les pages jaunes de _Jade_ grâce au comportement présent dans le fichier *YellowSetup.java* pour pouvoir par la suite être détecter par les autres agents. Une boucle s'enchaîne ensuite entre les comportements d'observation du graphe (présent dans le fichier *ObservationBehaviour.java* et de signal aux autres agents *SignalBehaviour.java*.

Dans le fichier *ObservationBehaviour.java*, les agents recupèrent les informations des cases environnantes et mettent à jour leur carte. Ils choisissent ensuite leur futur déplacement en conséquence. Une fois l'exploration de la carte terminé, ils passent dans un mode de chasse à la recherche de l'ennemi en fonction de la position de la trace de l'ennemi connu et des informations données par les autres agents via les signaux. Il est possible de bloquer l'ennemi lors de l'exploration de la carte si sa trace est répéré.

Dans le fichier *SignalBehaviour.java*, les agents envoient des informations par rapport à leurs position et aux informations qu'ils ont sur l'ennemi. Lors de l'exploration, les agents envoient aussi leur carte si des agents à proximité ont été détéctés pour accélerer l'exploration entière de la carte par tous les agents. Ce comportement permet aussi de récuperer les informations des autres agents sur leurs positions et les informations qu'ils ont sur l'ennemi pour pouvoir par la suite choisir la prochaine case à explorer dans le comportement d'observation.

**Voici des exemples de cartes essayées au cours du projet :**

**Arbre : 2 ennemis et 3 agents**

![Exemple sur Arbre]()

**Cycle : 1 ennemi et 5 agents**

![Exemple sur Cycle]()
