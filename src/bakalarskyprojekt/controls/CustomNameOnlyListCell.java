/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controls;


import javafx.scene.control.ListCell;

/**
 *
 * @author Kofola
 * @param <T> Class that implement interface namedComponent
 */
public class CustomNameOnlyListCell<T extends NamedComponent> extends ListCell<T> {

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            String text = item.getName(); // get text from item
            setText(text);
        }
    }

}
