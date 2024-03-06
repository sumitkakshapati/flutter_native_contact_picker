import Flutter
import UIKit
import ContactsUI

public class FlutterNativeContactPickerPlugin: NSObject, FlutterPlugin {

    var _delegate: PickerHandler?;
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_native_contact_picker", binaryMessenger: registrar.messenger())
    let instance = FlutterNativeContactPickerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
          if("selectContact" == call.method) {
            if(_delegate != nil) {
                _delegate!.result(FlutterError(code: "multiple_requests", message: "Cancelled by a second request.", details: nil));
                _delegate = nil;
              }

              if #available(iOS 9.0, *){
                  _delegate = SinglePickerHandler(result: result);
                  let contactPicker = CNContactPickerViewController()
                  contactPicker.delegate = _delegate
                  contactPicker.displayedPropertyKeys = [CNContactPhoneNumbersKey]
                  
                  // find proper keyWindow
                  var keyWindow: UIWindow? = nil
                  if #available(iOS 13, *) {
                      keyWindow = UIApplication.shared.connectedScenes.filter {
                          $0.activationState == .foregroundActive
                      }.compactMap { $0 as? UIWindowScene
                      }.first?.windows.filter({ $0.isKeyWindow}).first
                  } else {
                      keyWindow = UIApplication.shared.keyWindow
                  }
                  
                  let viewController = keyWindow?.rootViewController
                  viewController?.present(contactPicker, animated: true, completion: nil)
              }
          }
           else
              {
                  result(FlutterMethodNotImplemented)
              }
        }
}

class PickerHandler: NSObject, CNContactPickerDelegate  {
    var result: FlutterResult;
    
    required init(result: @escaping FlutterResult) {
        self.result = result
        super.init()
    }
    
    
    @available(iOS 9.0, *)
    public func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
        result(nil)
    }
}

class SinglePickerHandler: PickerHandler {
    @available(iOS 9.0, *)
    public func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {

        var data = Dictionary<String, Any>()
        data["fullName"] = CNContactFormatter.string(from: contact, style: CNContactFormatterStyle.fullName)

        let numbers: Array<String> = contact.phoneNumbers.compactMap { $0.value.stringValue as String }
        data["phoneNumbers"] = numbers

        result(data)
    }
}

