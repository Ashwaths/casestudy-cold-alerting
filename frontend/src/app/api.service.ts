import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Incident, ReadingPayload, TruckStatus } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;
  activeIncidents() { return this.http.get<Incident[]>(`${this.base}/incidents/active`); }
  truckStatus(id: string) { return this.http.get<TruckStatus>(`${this.base}/trucks/${encodeURIComponent(id)}/status`); }
  submitReading(payload: ReadingPayload) { return this.http.post(`${this.base}/sensors/readings`, payload); }
}
