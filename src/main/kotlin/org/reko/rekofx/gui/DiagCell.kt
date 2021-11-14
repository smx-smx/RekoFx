package org.reko.rekofx.gui

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.reko.rekofx.services.RekoEvent

class DiagCell : ListCell<RekoEvent>() {
    @FXML
    private lateinit var icon:ImageView

    @FXML
    private lateinit var text:Label

    init {
        val thisCell = this
        FXMLLoader(javaClass.getResource("diag_cell.fxml")).apply {
            setController(thisCell)
            setRoot(thisCell)
            load()
        }
    }

    override fun updateItem(itm: RekoEvent?, empty: Boolean) {
        super.updateItem(itm, empty)
        if(itm == null || empty){
            contentDisplay = ContentDisplay.TEXT_ONLY
            return
        }

        val imgName = when (itm.type) {
            "info" -> "info.png"
            "warning" -> "warning.png"
            "error" -> "error.png"
            else -> null
        }
        if(imgName != null){
            icon.image = Image(javaClass.getResourceAsStream("img/$imgName"))
        }
        text.text = itm.message

        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }
}

class DiagCellFactory : javafx.util.Callback<ListView<RekoEvent>, ListCell<RekoEvent>> {
    override fun call(param: ListView<RekoEvent>?): ListCell<RekoEvent> {
        return DiagCell()
    }
}