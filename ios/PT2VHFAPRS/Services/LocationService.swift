import CoreLocation
import Foundation

/// GPS do iPhone. Com o rastreador ligado e permissão "Sempre", continua em segundo plano.
final class LocationService: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published private(set) var location: CLLocation?
    @Published private(set) var authorization: CLAuthorizationStatus = .notDetermined

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        manager.distanceFilter = 20
        manager.activityType = .automotiveNavigation
        authorization = manager.authorizationStatus
    }

    func start(background: Bool) {
        if manager.authorizationStatus == .notDetermined { manager.requestWhenInUseAuthorization() }
        if background && manager.authorizationStatus == .authorizedWhenInUse { manager.requestAlwaysAuthorization() }
        let allowBg = background && manager.authorizationStatus == .authorizedAlways
        manager.allowsBackgroundLocationUpdates = allowBg
        manager.showsBackgroundLocationIndicator = allowBg
        manager.pausesLocationUpdatesAutomatically = !background
        manager.startUpdatingLocation()
    }

    func stop() {
        manager.allowsBackgroundLocationUpdates = false
        manager.stopUpdatingLocation()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorization = manager.authorizationStatus
        if authorization == .authorizedWhenInUse || authorization == .authorizedAlways {
            manager.startUpdatingLocation()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let last = locations.last, last.horizontalAccuracy >= 0, last.horizontalAccuracy < 200 else { return }
        location = last
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {}
}
