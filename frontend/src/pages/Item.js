import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";

export default function Item() {
  const { id } = useParams();
  const [rows,setRows] = useState([]);
  const [loading,setLoading] = useState(true);
  const [err,setErr]=useState("");

  const endpoint = `/distributors/item/${id}`;

  useEffect(()=> {
    (async ()=> {
      try {
        const r = await fetch(endpoint);
        if(!r.ok) throw new Error(`HTTP ${r.status}`);
        setRows(await r.json());
      } catch(e){ setErr(e.message); }
      finally{ setLoading(false); }
    })();
  }, [endpoint]);

  if (loading) return <div className="alert alert-info">Loading…</div>;
  if (err) return <div className="alert alert-danger">Error: {err}</div>;
  if (!rows.length) return <div className="alert alert-warning">No data.</div>;

  const cols = Object.keys(rows[0]);
  return (
    <div className="container py-4">
      <h1 className="mb-4">Distributor Prices for Item #{id}</h1>
      <div className="mb-3">
        <table className="table table-striped">
          <thead><tr>{cols.map(c=> <th key={c}>{c}</th>)}</tr></thead>
          <tbody>{rows.map((r,i)=><tr key={i}>{cols.map(c=> <td key={c}>{String(r[c])}</td>)}</tr>)}</tbody>
        </table>
    </div>
    </div>
  );
}
