import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:video_player/src/config.dart';
import 'package:video_player/src/models.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('legacy four-color settings default to 100 percent font scale', () {
    final settings = SubtitleSettings.fromJson(const [0xFFFFFFFF, 0xFF000000, 0, 0]);

    expect(settings.fontScalePercent, 100);
    expect(settings.toJson(), const [0xFFFFFFFF, 0xFF000000, 0, 0, 100]);
  });

  test('font scale round-trips with subtitle colors', () {
    const settings = SubtitleSettings(
      foregroundColor: Colors.white,
      backgroundColor: Colors.black,
      windowColor: Colors.transparent,
      edgeColor: Colors.black,
      fontScalePercent: 80,
    );

    expect(SubtitleSettings.fromJson(settings.toJson()), settings);
  });

  test('invalid persisted font scale falls back to 100 percent', () {
    final settings = SubtitleSettings.fromJson(const [0xFFFFFFFF, 0xFF000000, 0, 0, 0xFFFFFFFF]);

    expect(settings.fontScalePercent, 100);
  });

  test('new installs receive a five-value default subtitle configuration', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();

    expect(PlayerConfig.getSubtitleSettings(prefs), const [0xFFFFFFFF, 0xFF000000, 0, 0, 100]);
  });
}
