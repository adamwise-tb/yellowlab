import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function CreateDistributor() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setErr("");
    try {
      const res = await fetch("/distributors", {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        body: JSON.stringify({ name })
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      navigate("/");
    } catch (e) { setErr(e.message); }
    finally { setLoading(false); }
  };

  return (
    <div className="container py-4">
      <h1 className="mb-3">Create Distributor</h1>
      {err && <div className="alert alert-danger">{err}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Name</label>
          <input className="form-control" value={name}
                 onChange={(e)=>setName(e.target.value)} required />
        </div>
        <button className="btn btn-primary" disabled={loading}>
          {loading ? "Creating…" : "Create"}
        </button>
      </form>
    </div>
  );
}
