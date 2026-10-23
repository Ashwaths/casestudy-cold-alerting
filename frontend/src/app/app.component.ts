import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ApiService } from './api.service';
import { Incident, TruckStatus } from './models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe, HttpClientModule],
  template: `
  <main class="page">
    <header><div><p class="eyebrow">NIMBUS FREIGHT · OPERATIONS</p><h1>Cold-chain monitor</h1>
      <p class="sub">Sustained temperature excursion detection</p></div>
      <button (click)="refresh()">Refresh data</button></header>
    <section class="stats">
      <article><span>Active incidents</span><strong>{{ incidents.length }}</strong></article>
      <article><span>Threshold</span><strong>8°C</strong></article>
      <article><span>Incident trigger</span><strong>15 min</strong></article>
      <article><span>Recovery window</span><strong>10 min</strong></article>
    </section>
    <section class="panel">
      <div class="panel-head"><div><h2>Truck status</h2><p>Look up latest telemetry for a truck</p></div>
      <div class="search"><input [(ngModel)]="truckId" aria-label="Truck ID"><button (click)="loadTruck()">Search</button></div></div>
      <p class="error" *ngIf="error">{{error}}</p>
      <div class="truck" *ngIf="truck">
        <div><small>Truck</small><h3>{{truck.truckId}}</h3><p>{{truck.registrationNumber}} · {{truck.customerName}}</p></div>
        <div><small>Latest temperature</small><h3 [class.hot]="truck.latestTemperature !== null && truck.latestTemperature > 8">
          {{truck.latestTemperature === null ? '—' : (truck.latestTemperature | number:'1.1-1') + '°C'}}</h3>
          <p>{{truck.latestReadingAt ? (truck.latestReadingAt | date:'medium') : 'No reading yet'}}</p></div>
        <div><small>Telemetry</small><span class="pill">{{truck.telemetryState}}</span></div>
        <div><small>Incident</small><span class="pill" [class.danger]="truck.incidentStatus === 'ACTIVE'">{{truck.incidentStatus}}</span></div>
      </div>
    </section>
    <section class="panel">
      <div class="panel-head"><div><h2>Active incidents</h2><p>One active incident per sustained excursion</p></div></div>
      <table><thead><tr><th>Incident</th><th>Truck</th><th>Started</th><th>Peak</th><th>Status</th></tr></thead>
      <tbody><tr *ngFor="let i of incidents"><td>#{{i.incidentId}}</td><td>{{i.truck?.truckId}}</td>
      <td>{{i.startTime | date:'medium'}}</td><td class="hot">{{i.peakTemperature | number:'1.1-1'}}°C</td>
      <td><span class="pill danger">{{i.status}}</span></td></tr>
      <tr *ngIf="incidents.length === 0"><td colspan="5" class="empty">No active incidents</td></tr></tbody></table>
    </section>
    <footer>Demo dashboard · API-backed · Configure authentication before production use</footer>
  </main>`,
  styles: [`
  :host{display:block;min-height:100vh;background:#f3f6fa;color:#172338;font-family:Inter,Arial,sans-serif}
  .page{max-width:1180px;margin:auto;padding:36px 24px}header,.panel-head{display:flex;align-items:center;justify-content:space-between;gap:20px}
  header{margin-bottom:28px}.eyebrow{font-size:11px;letter-spacing:2px;color:#5974a1;font-weight:700}h1{font-size:32px;margin:8px 0}
  .sub,.panel-head p{color:#738097;margin:6px 0}.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:20px}
  .stats article,.panel{background:white;border:1px solid #e4eaf2;border-radius:14px;padding:22px;box-shadow:0 5px 18px #25395a08}
  .stats span,small{display:block;color:#77849a;font-size:13px}.stats strong{display:block;font-size:26px;margin-top:12px}
  .panel{margin:18px 0}.panel h2{font-size:19px;margin:0}.panel-head{margin-bottom:22px}.search{display:flex;gap:8px}
  input{border:1px solid #d9e1ec;border-radius:8px;padding:10px 12px;font:inherit}button{border:0;background:#1d4ed8;color:white;padding:10px 16px;border-radius:8px;font-weight:600;cursor:pointer}
  .truck{display:grid;grid-template-columns:2fr 1.3fr 1fr 1fr;gap:16px;padding:18px;background:#f8faff;border-radius:10px}
  h3{margin:8px 0;font-size:21px}.truck p{color:#758198;font-size:13px;margin:4px 0}.hot{color:#d04435;font-weight:700}
  .pill{display:inline-block;padding:6px 9px;background:#e7f7ee;color:#177245;border-radius:20px;font-size:11px;font-weight:700}
  .danger{background:#ffebe8;color:#b42318}table{width:100%;border-collapse:collapse;text-align:left}th,td{padding:14px 10px;border-bottom:1px solid #edf0f5;font-size:13px}th{color:#78859a;font-weight:600}
  .empty{text-align:center;color:#8490a3;padding:30px}.error{color:#b42318}footer{text-align:center;color:#8792a5;font-size:12px;margin:28px}
  @media(max-width:760px){.stats{grid-template-columns:repeat(2,1fr)}.truck{grid-template-columns:1fr 1fr}header,.panel-head{align-items:flex-start;flex-direction:column}table{display:block;overflow:auto}}
  `]
})
export class AppComponent implements OnInit {
  private api = inject(ApiService);
  truckId = 'TRUCK-101';
  truck: TruckStatus | null = null;
  incidents: Incident[] = [];
  error = '';
  ngOnInit() { this.refresh(); }
  refresh() {
    this.error = '';
    this.api.activeIncidents().subscribe({next: x => this.incidents = x, error: () => this.error = 'Could not load incidents. Check that the backend is running.'});
    this.loadTruck();
  }
  loadTruck() {
    this.error = '';
    this.api.truckStatus(this.truckId).subscribe({next: x => this.truck = x, error: e => { this.truck = null; this.error = e?.error?.error || 'Could not load truck status.'; }});
  }
}
