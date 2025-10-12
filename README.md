# Text Zoom Animator

Application JavaFX pour créer des animations GIF de texte avec effet zoom/dézoom.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)
![Maven](https://img.shields.io/badge/Maven-3.x-red)

## ✨ Fonctionnalités

- 🎨 **Génération d'animations GIF haute qualité** (4K - 3840x2160)
- 🔤 **Sélection de police** parmi toutes les polices système installées
- 🎨 **Personnalisation de la couleur** (format hexadécimal)
- 📊 **Contrôle des échelles** (début, milieu, fin) pour l'effet zoom
- 🎬 **Nombre d'images configurable**
- ✂️ **Crop automatique** pour optimiser la taille du GIF
- 🌈 **Fond transparent** dans le GIF exporté
- 👁️ **Prévisualisation en temps réel** du texte avec police et couleur
- 🔤 **Support des caractères spéciaux** avec police de secours Arial
- 📝 **Historique des polices et couleurs** récemment utilisées
- 💾 **Sauvegarde des préférences** (dossier d'export, police, échelles, etc.)

## 📋 Prérequis

- Java 17 ou supérieur
- Maven 3.x
- JavaFX 21.0.1 (géré automatiquement par Maven)

## 🚀 Installation et exécution

### Cloner le projet
```bash
git clone https://github.com/Dero06440/text-zoom-animator.git
cd text-zoom-animator
```

### Compiler et exécuter
```bash
mvn clean javafx:run
```

### Créer un package
```bash
mvn clean package
```

## 🎯 Utilisation

1. **Texte** : Entrez le texte à animer
2. **Police** : Sélectionnez une police parmi celles installées sur votre système
3. **Couleur** : Entrez un code couleur hexadécimal (ex: ff00eb)
4. **Nombre d'images** : Définissez le nombre de frames de l'animation
5. **Échelles** :
   - **Début** : Taille initiale du texte (%)
   - **Milieu** : Taille au milieu de l'animation (%)
   - **Fin** : Taille finale du texte (%)
6. **Dossier d'export** : Choisissez où sauvegarder le GIF
7. **Nom du fichier** : Le nom est automatiquement généré depuis le texte
8. Cliquez sur **GO** pour générer l'animation

## 🏗️ Architecture

```
src/main/java/com/zoom/
├── MainApp.java                    # Point d'entrée de l'application
├── controller/
│   └── MainController.java        # Logique de contrôle
├── model/
│   └── AnimationSettings.java     # Modèle de données
├── util/
│   └── GifExporter.java           # Génération et export GIF
└── view/
    └── MainView.java              # Interface utilisateur
```

## 🔧 Technologies utilisées

- **JavaFX 21.0.1** - Framework UI
- **Maven** - Gestion des dépendances
- **animated-gif-lib** - Encodage GIF
- **Java Preferences API** - Sauvegarde des préférences

## 📝 Dépendances

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>com.madgag</groupId>
    <artifactId>animated-gif-lib</artifactId>
    <version>1.4</version>
</dependency>
```

## 🎨 Exemples de résultats

L'application génère des GIF avec :
- Résolution 4K (3840x2160)
- Fond transparent
- Crop automatique selon le contenu
- Durée d'animation personnalisable (~3 secondes par défaut)

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
- Signaler des bugs
- Proposer de nouvelles fonctionnalités
- Soumettre des pull requests

## 📄 Licence

Ce projet est sous licence MIT.

## 👤 Auteur

Créé avec ❤️ et l'aide de [Claude Code](https://claude.com/claude-code)

---

**Note** : Les GIF générés sont sauvegardés par défaut dans `C:\temp` (configurable dans l'interface).
