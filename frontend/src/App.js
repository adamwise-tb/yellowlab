import { useState } from "react";
import 'bootstrap/dist/css/bootstrap.min.css'; // Leveraging bootstrap to make the app look nice easilyimport { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";

// Add for routing through links
import Item from "./pages/Item";
import Distributor from "./pages/Distributor";

export default function App() {
    const [rows, setRows] = useState([]); // initialize state values to empty arrays
    const [loading, setLoading] = useState(false); // initialize state values to false
    const [active, setActive] = useState(null); // initialize button state

    // We'll use this function to ping the API we built with Java
    const apiRequest = async (path) => {
        setActive(path); // make the button blue
        setLoading(true); // This way we can create a spinning bar or something

        try {
            const req = await fetch(`/${path}`); // await bc remember: the API won't respond instantly!
            const data = await req.json();
            setRows(Array.isArray(data) ? data : []); // Once the data returns, IF rows exist, set them as the data, otherwise default to an empty array
        } finally {
            // Once complete, set loading to false, so we can remove the loading bar
            setLoading(false);
        }
    }

    // Dynamically determine how many columns are needed dependent on the data returned
    const cols = rows[0] ? Object.keys(rows[0]) : [] // if the first row exist, grab the keys as columns

    return (
      <Router>
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
                      {cols.map((c) => (
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

          <Routes>
            <Route path="/items/:id" element={<Item />} />
            <Route path="/distributors/:id" element={<Distributor />} />
          </Routes>
        </div>
      </Router>
    )
}