import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, catchError, finalize, forkJoin, of, switchMap, timer } from 'rxjs';
import { ApiService } from './api.service';
import { Incident, ReadingPayload, TruckRequest, TruckStatus } from './models';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe],
  template: `
  <main class="page">
    <section class="login-panel" *ngIf="!auth.isAuthenticated">
      <p class="eyebrow">NIMBUS FREIGHT · OPERATIONS</p>
      <h1>Sign in</h1>
      <p class="sub">Sign in to access cold-chain monitoring.</p>
      <form (ngSubmit)="login()">
        <label for="username">Username</label>
        <input id="username" name="username" [(ngModel)]="loginUsername" autocomplete="username" required>
        <label for="password">Password</label>
        <input id="password" name="password" type="password" [(ngModel)]="loginPassword" autocomplete="current-password" required>
        <button type="submit" [disabled]="busy">{{busy ? 'Signing in…' : 'Sign in'}}</button>
      </form>
      <p class="error" role="alert" *ngIf="loginError">{{loginError}}</p>
    </section>
    <ng-container *ngIf="auth.isAuthenticated">
    <header><div><p class="eyebrow">NIMBUS FREIGHT · OPERATIONS</p><h1>Cold-chain monitor</h1>
      <p class="sub">Live telemetry and sustained temperature excursion detection</p></div>
      <div class="account"><span>{{auth.currentUser}}</span><button (click)="refresh()">Refresh data</button>
        <button class="secondary" (click)="toggleSimulator()">{{simulator ? 'Stop simulator' : 'Start simulator'}}</button>
        <button class="secondary" (click)="logout()">Sign out</button></div></header>
    <p class="live" role="status">Live fleet refresh every 15 seconds
      <span *ngIf="simulator"> · Simulator running ({{simulatorMode.toLowerCase()}})</span></p>
    <section class="stats">
      <article><span>Active trucks</span><strong>{{ activeTruckCount }}</strong></article>
      <article><span>Active incidents</span><strong>{{ incidents.length }}</strong></article>
      <article><span>Threshold</span><strong>8°C</strong></article>
      <article><span>Incident trigger</span><strong>15 min</strong></article>
    </section>
    <section class="panel">
      <div class="panel-head"><div><h2>Fleet telemetry</h2><p>Latest readings, temperature analysis, and truck management</p></div>
        <button (click)="openCreate()">Add truck</button></div>
      <p class="error" role="alert" *ngIf="error">{{error}}</p>
      <p class="error" role="alert" *ngIf="simulatorError">{{simulatorError}}</p>
      <div class="form-panel" *ngIf="showTruckForm">
        <h3>{{editingTruckId ? 'Edit truck details' : 'Add truck'}}</h3>
        <form class="truck-form" (ngSubmit)="saveTruck()">
          <label>Truck ID<input name="formTruckId" [(ngModel)]="truckForm.truckId" [disabled]="!!editingTruckId" required maxlength="64"></label>
          <label>Registration number<input name="registrationNumber" [(ngModel)]="truckForm.registrationNumber" required maxlength="64"></label>
          <label>Customer<input name="customerName" [(ngModel)]="truckForm.customerName" required maxlength="160"></label>
          <div class="form-actions"><button type="submit" [disabled]="savingTruck">{{savingTruck ? 'Saving…' : 'Save truck'}}</button>
            <button type="button" class="secondary" (click)="closeTruckForm()">Cancel</button></div>
        </form>
        <p class="error" role="alert" *ngIf="formError">{{formError}}</p>
      </div>
      <div class="table-wrap">
      <table><thead><tr><th>Truck</th><th>Customer</th><th>Latest temperature</th><th>Telemetry</th><th>Incident</th><th>Status</th><th>Actions</th></tr></thead>
      <tbody><tr *ngFor="let truck of trucks">
        <td><strong>{{truck.truckId}}</strong><small>{{truck.registrationNumber}}</small></td>
        <td>{{truck.customerName}}</td>
        <td><strong [class.hot]="truck.latestTemperature !== null && truck.latestTemperature > 8">
          {{truck.latestTemperature === null ? '—' : (truck.latestTemperature | number:'1.1-1') + '°C'}}</strong>
          <small>{{truck.latestReadingAt ? (truck.latestReadingAt | date:'short') : 'No reading yet'}}</small></td>
        <td><span class="pill" [class.warning]="truck.telemetryState === 'STALE'">{{truck.telemetryState}}</span></td>
        <td><span class="pill" [class.danger]="truck.incidentStatus === 'ACTIVE'">{{truck.incidentStatus}}</span></td>
        <td>{{truck.truckStatus}}</td>
        <td class="actions"><button class="small secondary" (click)="editTruck(truck)">Edit</button>
          <button *ngIf="truck.truckStatus === 'ACTIVE'" class="small secondary" (click)="archiveTruck(truck)">Archive</button>
          <button *ngIf="truck.truckStatus === 'INACTIVE'" class="small secondary" (click)="activateTruck(truck)">Activate</button>
          <button class="small delete" (click)="permanentlyDeleteTruck(truck)">Delete</button></td>
      </tr>
      <tr *ngIf="trucks.length === 0"><td colspan="7" class="empty">No trucks yet. Add a truck to start monitoring.</td></tr></tbody></table>
      </div>
    </section>
    <section class="panel">
      <div class="panel-head"><div><h2>Active incidents</h2><p>One active incident per sustained excursion</p></div></div>
      <div class="table-wrap"><table><thead><tr><th>Incident</th><th>Truck</th><th>Started</th><th>Peak</th><th>Status</th></tr></thead>
      <tbody><tr *ngFor="let i of incidents"><td>#{{i.incidentId}}</td><td>{{i.truck?.truckId}}</td>
      <td>{{i.startTime | date:'medium'}}</td><td class="hot">{{i.peakTemperature | number:'1.1-1'}}°C</td>
      <td><span class="pill danger">{{i.status}}</span></td></tr>
      <tr *ngIf="incidents.length === 0"><td colspan="5" class="empty">No active incidents</td></tr></tbody></table></div>
    </section>
    <footer>Authenticated operations dashboard · TLS protected</footer>
    </ng-container>
  </main>`,
  styles: [`
  :host{display:block;min-height:100vh;background:#f3f6fa;color:#172338;font-family:Inter,Arial,sans-serif}
  .login-panel{max-width:440px;margin:10vh auto;background:white;border:1px solid #e4eaf2;border-radius:14px;padding:32px;box-shadow:0 5px 18px #25395a08}
  .login-panel form{display:grid;gap:10px;margin-top:24px}.login-panel label,.truck-form label{font-size:13px;font-weight:600;color:#526078}
  .login-panel input{width:100%;margin-bottom:8px}.login-panel button{margin-top:8px}
  .page{max-width:1380px;margin:auto;padding:36px 24px}header,.panel-head{display:flex;align-items:center;justify-content:space-between;gap:20px}
  .account{display:flex;align-items:center;gap:10px;flex-wrap:wrap}.account span{font-size:13px;color:#526078}
  header{margin-bottom:8px}.eyebrow{font-size:11px;letter-spacing:2px;color:#5974a1;font-weight:700}h1{font-size:32px;margin:8px 0}
  .sub,.panel-head p{color:#738097;margin:6px 0}.live{font-size:12px;color:#526078;margin:10px 0 18px}
  .stats{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:20px}
  .stats article,.panel{background:white;border:1px solid #e4eaf2;border-radius:14px;padding:22px;box-shadow:0 5px 18px #25395a08}
  .stats span,small{display:block;color:#77849a;font-size:12px}.stats strong{display:block;font-size:26px;margin-top:12px}
  .panel{margin:18px 0}.panel h2{font-size:19px;margin:0}.panel-head{margin-bottom:22px}
  input{border:1px solid #d9e1ec;border-radius:8px;padding:10px 12px;font:inherit}button{border:0;background:#1d4ed8;color:white;padding:10px 16px;border-radius:8px;font-weight:600;cursor:pointer}
  button:disabled{opacity:.6;cursor:wait}.secondary{background:#526078}.delete{background:#b42318}.small{padding:7px 9px;font-size:12px}
  .form-panel{background:#f8faff;border:1px solid #e4eaf2;border-radius:10px;padding:18px;margin-bottom:20px}.form-panel h3{margin:0 0 14px}
  .truck-form{display:grid;grid-template-columns:repeat(3,minmax(160px,1fr));gap:14px;align-items:end}
  .truck-form label{display:grid;gap:6px}.truck-form input{width:100%}.truck-form input:disabled{background:#edf0f5;color:#78859a}
  .form-actions{display:flex;gap:8px}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;text-align:left;min-width:920px}
  th,td{padding:13px 10px;border-bottom:1px solid #edf0f5;font-size:13px}th{color:#78859a;font-weight:600}
  td small{margin-top:4px}.actions{display:flex;gap:5px;flex-wrap:wrap}.hot{color:#d04435;font-weight:700}
  .pill{display:inline-block;padding:6px 9px;background:#e7f7ee;color:#177245;border-radius:20px;font-size:10px;font-weight:700}
  .danger{background:#ffebe8;color:#b42318}.warning{background:#fff4db;color:#925f00}
  .empty{text-align:center;color:#8490a3;padding:30px}.error{color:#b42318}footer{text-align:center;color:#8792a5;font-size:12px;margin:28px}
  @media(max-width:760px){.stats{grid-template-columns:repeat(2,1fr)}header,.panel-head{align-items:flex-start;flex-direction:column}.truck-form{grid-template-columns:1fr}}
  `]
})
export class AppComponent implements OnInit, OnDestroy {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private polling?: Subscription;
  private simulatorSubscription?: Subscription;
  loginUsername = '';
  loginPassword = '';
  loginError = '';
  busy = false;
  savingTruck = false;
  showTruckForm = false;
  editingTruckId: string | null = null;
  truckForm: TruckRequest = { truckId: '', registrationNumber: '', customerName: '' };
  trucks: TruckStatus[] = [];
  incidents: Incident[] = [];
  error = '';
  formError = '';
  simulatorError = '';
  simulator = false;
  simulatorMode: 'NORMAL' | 'EXCURSION' = 'NORMAL';

  get activeTruckCount(): number {
    return this.trucks.filter(truck => truck.truckStatus === 'ACTIVE').length;
  }

  ngOnInit() {
    this.auth.restoreSession().subscribe({
      next: username => { if (username) this.startPolling(); },
      error: () => this.loginError = 'Could not reach the sign-in service. Try again.'
    });
  }

  ngOnDestroy() {
    this.polling?.unsubscribe();
    this.simulatorSubscription?.unsubscribe();
  }

  login() {
    this.busy = true;
    this.loginError = '';
    this.auth.login(this.loginUsername, this.loginPassword).pipe(
      finalize(() => this.busy = false)
    ).subscribe({
      next: () => { this.loginPassword = ''; this.startPolling(); },
      error: error => this.loginError = error.status === 401
        ? 'Sign-in failed. Check your username and password.'
        : 'Sign-in service is unavailable. Try again.'
    });
  }

  logout() {
    this.auth.logout().subscribe({
      next: () => {
        this.polling?.unsubscribe();
        this.simulatorSubscription?.unsubscribe();
        this.simulator = false;
        this.trucks = [];
        this.incidents = [];
      },
      error: () => this.error = 'Could not sign out. Retry after checking your connection.'
    });
  }

  refresh() {
    this.loadFleet();
  }

  openCreate() {
    this.editingTruckId = null;
    this.truckForm = { truckId: '', registrationNumber: '', customerName: '' };
    this.formError = '';
    this.showTruckForm = true;
  }

  editTruck(truck: TruckStatus) {
    this.editingTruckId = truck.truckId;
    this.truckForm = {
      truckId: truck.truckId,
      registrationNumber: truck.registrationNumber,
      customerName: truck.customerName
    };
    this.formError = '';
    this.showTruckForm = true;
  }

  closeTruckForm() {
    this.showTruckForm = false;
    this.formError = '';
  }

  saveTruck() {
    this.savingTruck = true;
    this.formError = '';
    const request = this.editingTruckId
      ? this.api.updateTruck(this.truckForm)
      : this.api.createTruck(this.truckForm);
    request.pipe(finalize(() => this.savingTruck = false)).subscribe({
      next: () => { this.closeTruckForm(); this.loadFleet(); },
      error: error => this.formError = error?.error?.error || 'Could not save truck details.'
    });
  }

  archiveTruck(truck: TruckStatus) {
    if (!window.confirm(`Archive ${truck.truckId}? Telemetry and incident history will be preserved.`)) return;
    this.api.archiveTruck(truck.truckId).subscribe({
      next: () => this.loadFleet(),
      error: error => this.error = error?.error?.error || 'Could not archive truck.'
    });
  }

  activateTruck(truck: TruckStatus) {
    this.api.activateTruck(truck.truckId).subscribe({
      next: () => this.loadFleet(),
      error: error => this.error = error?.error?.error || 'Could not activate truck.'
    });
  }

  permanentlyDeleteTruck(truck: TruckStatus) {
    const confirmed = window.confirm(
      `Permanently delete ${truck.truckId} and all its telemetry, incidents, and alert history? This cannot be undone.`
    );
    if (!confirmed) return;
    this.api.deleteTruck(truck.truckId).subscribe({
      next: () => this.loadFleet(),
      error: error => this.error = error?.error?.error || 'Could not delete truck.'
    });
  }

  toggleSimulator() {
    if (this.simulator) {
      this.simulatorSubscription?.unsubscribe();
      this.simulator = false;
      this.simulatorError = '';
      return;
    }
    if (!this.trucks.some(truck => truck.truckStatus === 'ACTIVE')) {
      this.simulatorError = 'Add or activate a truck before starting the simulator.';
      return;
    }
    this.simulatorMode = window.confirm('Run the simulator in excursion mode (>8°C) to test alerts?')
      ? 'EXCURSION' : 'NORMAL';
    this.simulatorError = '';
    this.simulator = true;
    this.simulatorSubscription = timer(0, 15000).pipe(
      switchMap(() => {
        const activeTrucks = this.trucks.filter(truck => truck.truckStatus === 'ACTIVE');
        const readings = activeTrucks.map(truck => {
          const payload: ReadingPayload = {
            truckId: truck.truckId,
            temperature: this.simulatorMode === 'EXCURSION'
              ? 9.5 + Math.random()
              : 3.5 + Math.random() * 2,
            timestamp: new Date().toISOString()
          };
          return this.api.submitReading(payload);
        });
        return readings.length ? forkJoin(readings).pipe(
          catchError(() => { this.simulatorError = 'Simulator could not submit telemetry. Check the connection.'; return of(null); })
        ) : of(null);
      })
    ).subscribe(result => {
      if (result) {
        this.simulatorError = '';
        this.loadFleet();
      }
    });
  }

  private startPolling() {
    this.polling?.unsubscribe();
    this.polling = timer(0, 15000).pipe(
      switchMap(() => forkJoin({
        trucks: this.api.trucks(),
        incidents: this.api.activeIncidents()
      }).pipe(catchError(() => {
        this.error = 'Could not refresh fleet data. Check that the backend is running.';
        return of(null);
      })))
    ).subscribe(data => {
      if (data) {
        this.trucks = data.trucks;
        this.incidents = data.incidents;
        this.error = '';
      }
    });
  }

  private loadFleet() {
    this.error = '';
    forkJoin({ trucks: this.api.trucks(), incidents: this.api.activeIncidents() }).subscribe({
      next: data => { this.trucks = data.trucks; this.incidents = data.incidents; },
      error: () => this.error = 'Could not refresh fleet data. Check that the backend is running.'
    });
  }
}
