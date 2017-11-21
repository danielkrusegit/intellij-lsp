package com.github.gtache.lsp.requests

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.Utils
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.lsp4j.{DidChangeWatchedFilesParams, FileChangeType, FileEvent}

/**
  * Handles all file events (save, willSave, changed, etc)
  */
object FileEventManager {

  /**
    * Indicates that a document will be saved
    *
    * @param doc The document
    */
  def willSave(doc: Document): Unit = {
    val uri = Utils.VFSToURIString(FileDocumentManager.getInstance().getFile(doc))
    EditorEventManager.forUri(uri).foreach(e => e.willSave())
  }

  /**
    * Indicates that all documents will be saved
    */
  def willSaveAllDocuments(): Unit = {
    EditorEventManager.willSaveAll()
  }

  /**
    * Called when a file is changed. Notifies the server if this file was watched.
    *
    * @param file The file
    */
  def fileChanged(file: VirtualFile): Unit = {
    val uri: String = Utils.VFSToURIString(file)
    if (uri != null) {
      EditorEventManager.forUri(uri) match {
        case Some(m) => m.documentSaved()
          changedConfiguration(uri, FileChangeType.Changed, m.wrapper)
        case None => changedConfiguration(uri, FileChangeType.Changed)
      }
    }
  }

  /**
    * Called when a file is moved. Notifies the server if this file was watched.
    *
    * @param file The file
    */
  def fileMoved(file: VirtualFile): Unit = {

  }

  /**
    * Called when a file is deleted. Notifies the server if this file was watched.
    *
    * @param file The file
    */
  def fileDeleted(file: VirtualFile): Unit = {
    val uri = Utils.VFSToURIString(file)
    if (uri != null) {
      changedConfiguration(uri, FileChangeType.Deleted)
    }
  }

  /**
    * Called when a file is renamed. Notifies the server if this file was watched.
    *
    * @param oldV The old file name
    * @param newV the new file name
    */
  def fileRenamed(oldV: String, newV: String): Unit = {

  }

  /**
    * Called when a file is created. Notifies the server if needed.
    *
    * @param file The file
    */
  def fileCreated(file: VirtualFile): Unit = {
    val uri = Utils.VFSToURIString(file)
    if (uri != null) {
      changedConfiguration(uri, FileChangeType.Created)
    }
  }

  private def changedConfiguration(uri: String, typ: FileChangeType, wrapper: LanguageServerWrapper = null): Unit = {
    import scala.collection.JavaConverters._
    Utils.pool(() => {
      val event = new FileEvent(uri, FileChangeType.Changed)
      val params = new DidChangeWatchedFilesParams(Seq(event).asJava)
      PluginMain.getAllServerWrappers.foreach(w => if (w != wrapper) w.getRequestManager.didChangeWatchedFiles(params))
    })
  }

}