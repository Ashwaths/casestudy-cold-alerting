import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Incident, ReadingPayload, TruckRequest, TruckStatus } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;
  trucks() { return this.http.get<TruckStatus[]>(`${this.base}/trucks`); }
  activeIncidents() { return this.http.get<Incident[]>(`${this.base}/incidents/active`); }
  truckStatus(id: string) { return this.http.get<TruckStatus>(`${this.base}/trucks/${encodeURIComponent(id)}/status`); }
  createTruck(truck: TruckRequest) { return this.http.post<TruckStatus>(`${this.base}/trucks`, truck); }
  updateTruck(truck: TruckRequest) {
    return this.http.put<TruckStatus>(`${this.base}/trucks/${encodeURIComponent(truck.truckId)}`, truck);
  }
  archiveTruck(id: string) {
    return this.http.post<TruckStatus>(`${this.base}/trucks/${encodeURIComponent(id)}/archive`, {});
  }
  activateTruck(id: string) {
    return this.http.post<TruckStatus>(`${this.base}/trucks/${encodeURIComponent(id)}/activate`, {});
  }
  deleteTruck(id: string) { return this.http.delete<void>(`${this.base}/trucks/${encodeURIComponent(id)}`); }
  submitReading(payload: ReadingPayload) { return this.http.post(`${this.base}/sensors/readings`, payload); }
}
