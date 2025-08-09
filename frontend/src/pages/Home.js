import { useState } from "react"; // We use this to store the "state" of the app, dependent on what buttons are clicked
import { Link } from "react-router-dom";

export default function Home() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [active, setActive] = useState(null);

  const apiRequest = async (path) => {
    setActive(path);
    setLoading(true);
    try {
      const req = await fetch(`/${path}`);
      const data = await req.json();
      setRows(Array.isArray(data) ? data : []);
    } finally {
      setLoading(false);
    }
  };

  const cols = rows[0] ? Object.keys(rows[0]) : [];

  return (
    <div className="container py-4">
      <h1 className="mb-4">Adam's Backend Code Challenge</h1>
      <div className="mb-3">
        <button
          className={`btn me-2 ${active === "items" ? "btn-primary" : "btn-secondary"}`}
          onClick={() => apiRequest("items")}
        >
          Items
        </button>
        <button
          className={`btn ${active === "distributors" ? "btn-primary" : "btn-secondary"}`}
          onClick={() => apiRequest("distributors")}
        >
          Distributors
        </button>
      </div>

      {loading && <div className="alert alert-info">Loading…</div>}

      {!loading && rows.length > 0 && (
        <table className="table table-striped">
          <thead>
            <tr>{cols.map(c => <th key={c}>{c}</th>)}</tr>
          </thead>
          <tbody>
            {rows.map((row, i) => (
              <tr key={i}>
                {cols.map(c => (
                  <td key={c}>
                    {c === "name" ? (
                      <Link to={`/${active}/${row.id}`}>{row[c]}</Link>
                    ) : (
                      row[c]
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
