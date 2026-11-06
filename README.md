# Luma Launcher — base Android 0.1.0

Projet natif Java pour l’accueil clair de Josias, inspiré des maquettes validées.

## Inclus

- Sélection comme application d’accueil Android.
- Applications réellement installées, triées et recherchables ; toucher pour lancer, appui long pour leurs paramètres.
- Horloge/date locales, surfaces translucides, choix d’une photo conservé entre les ouvertures.
- Aperçu du verrouillage clairement identifié. Aucun remplacement du verrouillage sécurisé.

Cette base n’est pas encore une reproduction complète des maquettes. Elle conserve les icônes Android d’origine. Le détourage du sujet, l’horloge derrière le sujet, le flou réel, les widgets météo, le dock et le réagencement restent à développer. L’aperçu de verrouillage n’a ni notifications réelles ni authentification personnalisée.

## Compiler

Ouvrir le dossier dans Android Studio avec JDK 17, SDK Android 35 et Gradle 8.9. La distribution Gradle n’est pas incluse : installer Gradle 8.9 ou générer le wrapper avec `gradle wrapper --gradle-version 8.9`.

Commande : `gradle lintDebug assembleDebug`.

Le workflow `.github/workflows/android.yml` installe les outils et produit un APK de test téléchargeable dans GitHub Actions. La compilation n’a pas été exécutée dans l’environnement de préparation, qui ne possède ni SDK Android ni Gradle. Le premier passage du workflow doit donc valider cette base avant installation.

## Mettre sur GitHub

Dépôt du projet : https://github.com/Josi-Cell-ctrl11/Lumia. Le projet conserve le nom d’application Luma Launcher. Les sources et le workflow doivent être à la racine du dépôt, pas dans une archive ZIP.

## Tester sur Infinix

Après compilation, installer l’APK de test. Sélectionner Luma dans les applications d’accueil par défaut. Vérifier la recherche, le lancement, les retours à l’accueil, le fond après redémarrage et les marges système. Revenir au launcher Infinix dans les paramètres pour quitter Luma. Le code et l’empreinte système ne sont pas modifiés.

L’APK debug sert aux essais. Une signature permanente et sa sauvegarde privée devront être établies avant une distribution durable. Ne jamais mettre de clé privée dans ce dépôt.
