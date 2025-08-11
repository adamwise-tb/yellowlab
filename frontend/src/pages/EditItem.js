import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

export default function EditItem() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErr(null);
    try {
      const res = await fetch(`/items/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 'name': name })
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      navigate(`/`); // go back to detail page
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-4">
      <h1 className="mb-4">Edit Item #{id}</h1>
      {err && <div className="alert alert-danger">{err}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">New Name</label>
          <input
            type="text"
            className="form-control"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <button className="btn btn-primary" disabled={loading}>
          {loading ? "Saving…" : "Save"}
        </button>
      </form>
    </div>
  );
}
