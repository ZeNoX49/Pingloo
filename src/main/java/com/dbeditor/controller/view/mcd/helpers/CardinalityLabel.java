// package com.dbeditor.controller.view.mcd.helpers;

// import com.dbeditor.model.mcd.Cardinality;
// import com.dbeditor.util.ThemeManager;

// import javafx.scene.control.Label;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;

// /**
//  * Label visuel pour afficher une cardinalité sur un lien MCD
//  * Positionné près de l'entité concernée
//  */
// public class CardinalityLabel extends Label {
//     private static final ThemeManager T_M = ThemeManager.getInstance();
    
//     private Cardinality cardinality;
//     private double offsetX = 10; // Distance par rapport au lien
//     private double offsetY = -15;
    
//     public CardinalityLabel(Cardinality cardinality) {
//         this.cardinality = cardinality;
//         setupLabel();
//     }
    
//     /**
//      * Configure l'apparence du label
//      */
//     private void setupLabel() {
//         // Texte de la cardinalité
//         setText(cardinality.toString());
        
//         // Style
//         setFont(Font.font("System", FontWeight.BOLD, 13));
//         updateStyle();
        
//         // Padding et fond
//         setStyle(getStyle() + 
//             "-fx-padding: 2 4 2 4; " +
//             "-fx-background-color: " + T_M.getTheme().getBackgroundColor() + "; " +
//             "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
//             "-fx-border-width: 1; " +
//             "-fx-border-radius: 3; " +
//             "-fx-background-radius: 3;");
        
//         // Rendre le label cliquable mais transparent aux événements souris
//         setMouseTransparent(true);
//     }
    
//     /**
//      * Met à jour le style (pour changement de thème)
//      */
//     public void updateStyle() {
//         // Couleur selon le type de cardinalité
//         String textColor = getCardinalityColor();
//         setTextFill(Color.web(textColor));
        
//         setStyle(
//             "-fx-text-fill: " + textColor + "; " +
//             "-fx-font-weight: bold; " +
//             "-fx-padding: 2 4 2 4; " +
//             "-fx-background-color: " + T_M.getTheme().getBackgroundColor() + "; " +
//             "-fx-border-color: " + textColor + "; " +
//             "-fx-border-width: 1; " +
//             "-fx-border-radius: 3; " +
//             "-fx-background-radius: 3;"
//         );
//     }
    
//     /**
//      * Retourne une couleur en fonction du type de cardinalité
//      */
//     private String getCardinalityColor() {
//         // 1,1 ou 1,n : rouge (obligatoire)
//         if (cardinality.isMandatory()) {
//             return "#d9534f"; // Rouge
//         }
//         // 0,1 : bleu (optionnel, un seul)
//         else if (cardinality.isOptional() && cardinality.isOne()) {
//             return "#0275d8"; // Bleu
//         }
//         // 0,n : vert (optionnel, multiple)
//         else {
//             return "#5cb85c"; // Vert
//         }
//     }
    
//     /**
//      * Positionne le label le long d'une ligne
//      * @param entityX position X de l'entité
//      * @param entityY position Y de l'entité
//      * @param assocX position X de l'association
//      * @param assocY position Y de l'association
//      * @param nearEntity true pour placer près de l'entité, false pour près de l'association
//      */
//     public void positionAlongLine(double entityX, double entityY, 
//                                   double assocX, double assocY, 
//                                   boolean nearEntity) {
//         // Calculer le vecteur de direction
//         double dx = assocX - entityX;
//         double dy = assocY - entityY;
//         double length = Math.sqrt(dx * dx + dy * dy);
        
//         if (length == 0) {
//             setLayoutX(entityX + offsetX);
//             setLayoutY(entityY + offsetY);
//             return;
//         }
        
//         // Normaliser le vecteur
//         dx /= length;
//         dy /= length;
        
//         // Position le long de la ligne
//         double ratio = nearEntity ? 0.15 : 0.85; // 15% depuis l'entité ou 85% vers l'association
//         double lineX = entityX + dx * length * ratio;
//         double lineY = entityY + dy * length * ratio;
        
//         // Vecteur perpendiculaire pour décaler le label
//         double perpX = -dy;
//         double perpY = dx;
        
//         // Position finale avec offset perpendiculaire
//         setLayoutX(lineX + perpX * offsetX);
//         setLayoutY(lineY + perpY * offsetY);
//     }
    
//     /**
//      * Positionne automatiquement près d'une entité
//      */
//     public void positionNearEntity(double entityX, double entityY, 
//                                    double entityWidth, double entityHeight) {
//         setLayoutX(entityX + entityWidth / 2 + offsetX);
//         setLayoutY(entityY + entityHeight / 2 + offsetY);
//     }
    
//     /**
//      * Met à jour la cardinalité et le texte
//      */
//     public void setCardinality(Cardinality cardinality) {
//         this.cardinality = cardinality;
//         setText(cardinality.toString());
//         updateStyle();
//     }
    
//     public Cardinality getCardinality() {
//         return cardinality;
//     }
    
//     public void setOffset(double x, double y) {
//         this.offsetX = x;
//         this.offsetY = y;
//     }
// }