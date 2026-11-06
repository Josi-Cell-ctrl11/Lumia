# Luma Launcher 0.2.0

Accueil Android clair inspiré des maquettes validées : calendrier, météo de Cotonou, icônes colorées, surfaces translucides avec diffusion du fond, recherche, pages d’applications par glissement et dock de quatre applis personnalisables.

## Profondeur et personnalisation

Appui long sur l’accueil → Personnalisation. Choisir une photo, puis :

- **Personne** : détourage local automatique par ML Kit. Le modèle est inclus dans l’APK ; la photo n’est pas envoyée à un serveur. Résultat variable selon la photo.
- **Paysage / objet** : peindre la zone du sujet au pinceau. Basculer vers la gomme pour corriger, puis enregistrer. Le masque suit exactement le cadrage de la photo.
- Régler la hauteur de l’horloge et activer/désactiver la profondeur. Les réglages et le masque sont conservés.

L’aperçu du verrouillage affiche le sujet devant l’heure. Un paysage vectoriel de démonstration montre cet effet même sans photo. Le fond animé Luma peut être choisi dans le sélecteur système pour appliquer la composition au verrouillage lorsque XOS l’autorise. XOS peut afficher sa propre horloge par-dessus : une appli ordinaire ne peut pas la supprimer. L’aperçu dans Luma n’est pas un verrouillage sécurisé ; le fond animé ne remplace ni le code ni l’empreinte. Les notifications et raccourcis du verrouillage réel restent ceux d’Infinix. Aucune notification fictive n’est affichée.

Les icônes proviennent des applications réellement installées, avec un cadre et un reflet assortis ; le launcher n’installe pas les applications Apple. Les deux widgets d’accueil ouvrent le calendrier et actualisent la météo. La météo utilise Open-Meteo (https://open-meteo.com/) pour Cotonou, sans localisation. En cas de coupure, la dernière mesure est identifiée. Le calendrier et la batterie utilisent les données du téléphone.

## Compilation et installation

JDK 17, Gradle 8.9, SDK 35 : `gradle lintDebug assembleDebug`. Le workflow GitHub produit `Luma-0.2.0-preview`. Ouvrir la compilation réussie dans Actions, télécharger l’archive puis extraire l’APK. L’APK est une version de test ; sa signature debug peut différer de la 0.1.0 issue d’un autre runner. Si Android refuse la mise à jour, désinstaller l’ancienne version de test (les réglages Luma seront effacés) puis installer la nouvelle. Une signature permanente reste à préparer avant distribution stable.

Dépôt : https://github.com/Josi-Cell-ctrl11/Lumia
