import { useEffect, useState } from 'react';

/**
 * The backend sends timestamps like "2026-07-01T15:00:00" — no
 * trailing "Z" and no offset. That's an ambiguous ISO string, and
 * `new Date(...)` parses anything without a zone marker as the
 * *browser's local time*, not the server's.
 *
 * Confirmed from application.properties:
 *   spring.jackson.time-zone=Asia/Dhaka
 *   spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Dhaka
 * So every timestamp the API returns already means Asia/Dhaka wall-clock
 * time (UTC+06:00, no DST — the offset never changes). Relying on the
 * *visitor's* browser timezone to interpret it only happens to work for
 * visitors who are themselves in Dhaka; for anyone else (or a browser/CI
 * environment set to UTC) it silently shifts every contest by the
 * difference between their zone and +06:00 — which is exactly the kind
 * of bug that makes a contest that should be live show up as "ended".
 *
 * This pins the offset explicitly instead of trusting the browser.
 */
export function parseServerDate(value) {
  if (!value) return new Date(NaN);
  const hasZone = /Z$|[+-]\d{2}:\d{2}$/.test(value);
  return new Date(hasZone ? value : `${value}+06:00`);
}

/**
 * Ticks once a second and returns the signed millisecond difference
 * between `targetTime` and now (negative once the target has passed).
 * Used to drive the "starts in" / "time remaining" chips.
 */
export function useCountdown(targetTime) {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  return targetTime - now;
}

export function formatDuration(ms) {
  const abs = Math.max(0, Math.abs(ms));
  const totalSeconds = Math.floor(abs / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  const pad = (n) => String(n).padStart(2, '0');

  if (days > 0) return `${days}d ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
}

/**
 * Classifies an exam against the current time into one of three
 * student-facing buckets, matching the Discover Your Exams filter.
 */
export function getContestStatus(exam, now = Date.now()) {
  const start = parseServerDate(exam.startTime).getTime();
  const end = start + (exam.duration || 0) * 60000;

  if (exam.status !== 'PUBLISHED') return 'draft';
  if (now < start) return 'upcoming';
  if (now >= start && now <= end) return 'live';
  return 'ended';
}
