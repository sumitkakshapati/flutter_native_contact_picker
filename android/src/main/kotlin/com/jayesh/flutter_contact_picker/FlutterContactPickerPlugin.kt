package com.jayesh.flutter_contact_picker

import android.annotation.SuppressLint
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.provider.ContactsContract
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry
import java.util.*

/** FlutterContactPickerPlugin */
public class FlutterContactPickerPlugin: FlutterPlugin, MethodCallHandler,
        ActivityAware, PluginRegistry.ActivityResultListener{
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private var pendingResult: Result? = null
  private  val PICK_CONTACT = 2015

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_native_contact_picker")
    channel.setMethodCallHandler(this);
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "selectContact") {
      if (pendingResult != null) {
        pendingResult!!.error("multiple_requests", "Cancelled by a second request.", null)
        pendingResult = null
      }
      pendingResult = result

      val i = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
      activity?.startActivityForResult(i, PICK_CONTACT)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(p0: ActivityPluginBinding) {
    this.activity = p0.activity

    p0.addActivityResultListener(this)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    this.activity = null
  }

  override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
    this.activity = activityPluginBinding.activity
    activityPluginBinding.addActivityResultListener(this)
  }

  override fun onDetachedFromActivity() {
    this.activity = null
  }

  @SuppressLint("Range")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (requestCode != PICK_CONTACT) {
      return false
    }
    if (resultCode != RESULT_OK) {
      pendingResult?.success(null)
      pendingResult = null
      return true
    }

    data?.data?.let { contactUri ->
      val cursor = activity!!.contentResolver.query(contactUri, null, null, null, null)
      cursor?.use {
        it.moveToFirst()
       // val phoneType = it.getInt(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
       // val customLabel = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL))
       // val label = ContactsContract.CommonDataKinds.Email.getTypeLabel(activity!!.resources, phoneType, customLabel) as String
        val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        val fullName = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
       // val phoneNumber = HashMap<String, Any>()
       // phoneNumber.put("number", number)
       // phoneNumber.put("label", label)
        val contact = HashMap<String, Any>()
        contact.put("fullName", fullName)
        contact.put("phoneNumbers", listOf(number))
        pendingResult?.success(contact)
        pendingResult = null
        return@use true
      }
    }

    pendingResult?.success(null)
    pendingResult = null
    return true
  }

  companion object {
    private const val PICK_CONTACT = 2015
  }
}