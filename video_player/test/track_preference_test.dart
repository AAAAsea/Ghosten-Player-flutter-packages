import 'package:flutter_test/flutter_test.dart';
import 'package:video_player/src/config.dart';
import 'package:video_player/src/models.dart';

MediaTrack track({
  required String id,
  String? label,
  String? name,
  String? mimeType = 'audio/aac',
  bool supported = true,
}) => MediaTrack.fromJson({
  'id': id,
  'selected': false,
  'supported': supported,
  'label': label,
  'mimeType': mimeType,
  'rate': null,
  'averageBitrate': null,
  'name': name,
  'type': 'audio',
});

void main() {
  group('TrackPreference.match', () {
    test('matches the same language across different track IDs', () {
      final preference = TrackPreference.fromTrack(track(id: '2', label: 'Mandarin', name: 'Chinese'));
      final tracks = [
        track(id: '2', label: 'English', name: 'English'),
        track(id: '5', label: 'Mandarin', name: 'Chinese'),
      ];

      expect(preference.match(tracks)?.id, '5');
    });

    test('does not use a reused ID when metadata identifies another track', () {
      final preference = TrackPreference.fromTrack(track(id: '2', label: 'Mandarin', name: 'Chinese'));
      final tracks = [track(id: '2', label: 'English', name: 'English')];

      expect(preference.match(tracks), isNull);
    });

    test('uses the ID when a track has no descriptive metadata', () {
      final preference = TrackPreference.fromTrack(track(id: '2'));
      final tracks = [track(id: '1'), track(id: '2')];

      expect(preference.match(tracks)?.id, '2');
    });

    test('ignores unsupported tracks', () {
      final preference = TrackPreference.fromTrack(track(id: '2', label: 'Mandarin', name: 'Chinese'));
      final tracks = [track(id: '5', label: 'Mandarin', name: 'Chinese', supported: false)];

      expect(preference.match(tracks), isNull);
    });

    test('disabled preference never selects a track', () {
      final tracks = [track(id: '1', label: 'English', name: 'English')];

      expect(TrackPreference.disabled().match(tracks), isNull);
    });
  });
}
