import Card from "./Card.jsx";

export default function Player({ name, cards }) {
    return (
        <div>
            <h4>{name}</h4>
            <div>
                {cards.map((c, i) => (
                    <Card key={i} value={c} />
                ))}
            </div>
        </div>
    );
}
