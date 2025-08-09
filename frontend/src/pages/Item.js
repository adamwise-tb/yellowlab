import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

export default function Item() {
  const { id } = useParams();              // /items/:id
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const r = await fetch(`/distributors/item/${id}`);
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        const data = await r.json();
        setRows(Array.isArray(data) ? data : []);
      } catch (e) {
        setErr(e.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  if (loading) return <div className="alert alert-info">Loading…</div>;
  if (err) return <div className="alert alert-danger">Error: {err}</div>;
  if (!rows.length) return <div className="alert alert-warning">No distributors for item #{id}</div>;

  const cols = Object.keys(rows[0]); // e.g., name, distributor_id, cost

  return (
    <div className="container py-3">
      <h2 className="mb-3">Item #{id} — Distributors</h2>
      <table className="table table-striped">
        <thead><tr>{cols.map(c => <th key={c}>{c}</th>)}</tr></thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i}>{cols.map(c => <td key={c}>{String(row[c])}</td>)}</tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
