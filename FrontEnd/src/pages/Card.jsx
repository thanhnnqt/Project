export default function Card({ value }) {
    return (
        <img
            src={`/cards/${value}.png`}
            alt={value}
            width={80}
            style={{ margin: "5px" }}
        />
    );
}
