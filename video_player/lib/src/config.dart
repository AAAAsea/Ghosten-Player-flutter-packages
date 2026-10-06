import 'dart:convert';

import 'package:collection/collection.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'models.dart';

class TrackPreference {
  const TrackPreference({required this.disabled, this.id, this.label, this.name, this.mimeType});

  factory TrackPreference.disabled() => const TrackPreference(disabled: true);

  factory TrackPreference.fromTrack(MediaTrack track) =>
      TrackPreference(disabled: false, id: track.id, label: track.label, name: track.name, mimeType: track.mimeType);

  factory TrackPreference.fromJson(Map<String, dynamic> json) => TrackPreference(
    disabled: json['disabled'] == true,
    id: json['id'] as String?,
    label: json['label'] as String?,
    name: json['name'] as String?,
    mimeType: json['mimeType'] as String?,
  );

  final bool disabled;
  final String? id;
  final String? label;
  final String? name;
  final String? mimeType;

  Map<String, dynamic> toJson() => {'disabled': disabled, 'id': id, 'label': label, 'name': name, 'mimeType': mimeType};

  MediaTrack? match(Iterable<MediaTrack> tracks) {
    if (disabled) return null;

    final supported = tracks.where((track) => track.supported).toList();
    if (supported.isEmpty) return null;

    final normalizedLabel = _normalize(label);
    final normalizedName = _normalize(name);
    final hasMetadata = normalizedLabel != null || normalizedName != null;

    final ranked = supported
        .map(
          (track) =>
              (track: track, score: _score(track, normalizedLabel: normalizedLabel, normalizedName: normalizedName)),
        )
        .sorted((a, b) => b.score.compareTo(a.score));

    final best = ranked.firstOrNull;
    if (best == null) return null;
    if (hasMetadata && best.score >= 30) return best.track;
    if (!hasMetadata && best.track.id == id) return best.track;
    return null;
  }

  int _score(MediaTrack track, {required String? normalizedLabel, required String? normalizedName}) {
    var score = 0;
    final trackLabel = _normalize(track.label);
    final trackName = _normalize(track.name);

    if (normalizedLabel != null && trackLabel == normalizedLabel) score += 50;
    if (normalizedName != null && trackName == normalizedName) score += 40;
    if (mimeType != null && track.mimeType == mimeType) score += 5;
    if (id != null && track.id == id) score += 2;
    return score;
  }

  static String? _normalize(String? value) {
    final normalized = value?.trim().toLowerCase();
    return normalized == null || normalized.isEmpty ? null : normalized;
  }
}

class PlayerConfig {
  static String _trackPreferenceKey(String type) => 'playerConfig.trackPreference.$type';

  static TrackPreference? getTrackPreference(SharedPreferences prefs, String type) {
    final value = prefs.getString(_trackPreferenceKey(type));
    if (value == null) return null;
    try {
      return TrackPreference.fromJson(jsonDecode(value) as Map<String, dynamic>);
    } on FormatException {
      return null;
    } on TypeError {
      return null;
    }
  }

  static Future<bool> setTrackPreference(SharedPreferences prefs, String type, TrackPreference preference) {
    return prefs.setString(_trackPreferenceKey(type), jsonEncode(preference.toJson()));
  }

  static int getExtensionRendererMode(SharedPreferences prefs) {
    return prefs.getInt('playerConfig.extensionRendererMode') ?? 1;
  }

  static void setExtensionRendererMode(SharedPreferences prefs, int mode) {
    prefs.setInt('playerConfig.extensionRendererMode', mode);
  }

  static int getFastForwardSpeed(SharedPreferences prefs) {
    return prefs.getInt('playerConfig.fastForwardSpeed') ?? 30;
  }

  static void setFastForwardSpeed(SharedPreferences prefs, int speed) {
    prefs.setInt('playerConfig.fastForwardSpeed', speed);
  }

  static bool getEnableDecoderFallback(SharedPreferences prefs) {
    return prefs.getBool('playerConfig.enableDecoderFallback') ?? false;
  }

  static void setEnableDecoderFallback(SharedPreferences prefs, bool enableDecoderFallback) {
    prefs.setBool('playerConfig.enableDecoderFallback', enableDecoderFallback);
  }

  static bool getShowThumbnails(SharedPreferences prefs) {
    return prefs.getBool('playerConfig.showThumbnails') ?? false;
  }

  static void setShowThumbnails(SharedPreferences prefs, bool showThumbnails) {
    prefs.setBool('playerConfig.showThumbnails', showThumbnails);
  }

  static List<int> getSubtitleSettings(SharedPreferences prefs) {
    return prefs.getString('playerConfig.subtitleSettings')?.split(',').map(int.parse).toList() ??
        [0xFFFFFFFF, 0xFF000000, 0, 0, 100];
  }

  static void setSubtitleSettings(SharedPreferences prefs, List<int> settings) {
    prefs.setString('playerConfig.subtitleSettings', settings.join(','));
  }
}
