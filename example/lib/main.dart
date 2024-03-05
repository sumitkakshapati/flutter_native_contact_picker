import 'package:flutter/material.dart';
import 'package:flutter_native_contact_picker/flutter_native_contact_picker.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final FlutterContactPicker _contactPicker = FlutterContactPicker();
  List<Contact>? _contacts;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Contact Picker Example App'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              MaterialButton(
                color: Colors.blue,
                child: const Text("Single"),
                onPressed: () async {
                  Contact? contact = await _contactPicker.selectContact();
                  setState(() {
                    _contacts = contact == null ? null : [contact];
                  });
                },
              ),
              if (_contacts != null)
                ..._contacts!.map(
                  (e) => Text(e.toString()),
                )
            ],
          ),
        ),
      ),
    );
  }
}
