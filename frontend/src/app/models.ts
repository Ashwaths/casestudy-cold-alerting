export interface Incident {
  incidentId: number;
  truck: { truckId: string };
  startTime: string;
  endTime: string | null;
  peakTemperature: number;
  status: string;
}
export interface TruckStatus {
  truckId: string;
  registrationNumber: string;
  customerName: string;
  latestTemperature: number | null;
  latestReadingAt: string | null;
  telemetryState: 'CURRENT' | 'STALE' | 'NO_DATA';
  incidentStatus: 'NORMAL' | 'ACTIVE';
}
export interface ReadingPayload {
  truckId: string;
  temperature: number;
  timestamp: string;
}
