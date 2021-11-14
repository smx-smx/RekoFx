package org.reko.rekofx.gui

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.reko.rekofx.services.RekoEvent
import org.reko.rekofx.services.RekoSession
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread

@Serializable
data class ProcedureInfo(
    val sProgram: String,
    val sAddress: String,
    val name: String
){
    override fun toString(): String {
        return "[$sAddress]: $name"
    }
}

class MainController : Initializable {
    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private lateinit var listView: ListView<ProcedureInfo>

    @FXML
    private lateinit var diagView: ListView<RekoEvent>

    @field:Inject
    private lateinit var stage: Stage

    @field:Inject
    private lateinit var reko:RekoSession

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        diagView.cellFactory = DiagCellFactory()
        reko.addEventListener {
            Platform.runLater {
                diagView.items.add(it)
            }
        }
    }

    @FXML
    private fun onHelloButtonClick() {
        Platform.runLater {
            val chosenFile = FileChooser().apply {
                title = "Select file to decompile"
                extensionFilters.addAll(
                    FileChooser.ExtensionFilter("All files", "*.*")
                )
            }.let { it.showOpenDialog(stage) } ?: return@runLater

            thread {
                val cmd = buildJsonObject {
                    put("cmd", "openFile")
                    put("filePath", chosenFile.path)
                }
                val reply = reko.sendCommand(cmd)
                Platform.runLater {
                    welcomeText.text = reply
                }
            }
        }
    }

    fun onListViewButtonClick(actionEvent: ActionEvent) {
        thread {
            buildJsonObject {
                put("cmd", "scanPrograms")
            }
            .let { reko.sendCommand(it) }
            .let { println(it) }

            val cmd = buildJsonObject {
                put("cmd", "getProcedureList")
            }
            val reply = reko.sendCommand(cmd)
            println(reply)

            val deser = ListSerializer(ProcedureInfo.serializer())
            val procs = Json.decodeFromString(deser, reply) as List<ProcedureInfo>
            procs.forEach {
                println(it)
            }
            Platform.runLater {
                listView.items = FXCollections.observableList(procs.toList())
            }
        }
    }

    fun onMouseClick(mouseEvent: MouseEvent) {
        val itm = listView.selectionModel.selectedItem ?: return

        Platform.runLater {
            Alert(Alert.AlertType.INFORMATION).apply {
                contentText = itm.toString()
            }.showAndWait()
        }
    }
}