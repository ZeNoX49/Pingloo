import os

def lister_fichiers(dir_path):
    liens = []
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            liens.append(os.path.join(root, file))
    return liens

# Exemple d'utilisation
dossier = "H:/Pingloo/src/main"
for lien in lister_fichiers(dossier):
    print(lien.replace("\\", "/").replace("H:/Pingloo/src/main", "https://raw.githubusercontent.com/ZeNoX49/Pingloo/splitUtilsView/src/main"))