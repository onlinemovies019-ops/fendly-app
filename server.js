import express from "express";

const PORT = Number(process.env.PORT || 3000);
const app = express();

app.get("/health", (_request, response) => {
  return response.json({status: "ok"});
});

app.all("/api/items", (_request, response) => {
  return response.status(410).json({error: "Public report APIs have been retired"});
});

app.all("/api/items/match", (_request, response) => {
  return response.status(410).json({error: "Public report matching has been retired"});
});

app.listen(PORT, () => {
  console.log(`Fendly private service listening on port ${PORT}`);
});
